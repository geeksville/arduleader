package com.geeksville.flight.lead

// Standard akka imports
import scala.concurrent.duration._
import com.geeksville.mavlink._
import com.geeksville.flight._
import com.geeksville.akka.InstrumentedActor
import com.geeksville.flight.wingman.Wingman
import com.geeksville.util.Counted
import com.geeksville.util.SystemTools
import org.mavlink.messages.MAVLinkMessage
import com.geeksville.shell.ScalaShell
import com.geeksville.shell.ScalaConsole
import gnu.io.NoSuchPortException
import com.geeksville.logback.Logging
import com.geeksville.flight.FlightLead
import com.geeksville.akka.MockAkka
import java.io._
import com.geeksville.mavserve.MavServe
import com.geeksville.mavlink.LogIncomingMavlink
import com.geeksville.gcsapi.Webserver
import com.geeksville.gcsapi.TempGCSModel
import com.geeksville.gcsapi.GCSAdapter
import akka.actor.Props
import com.geeksville.flight.EventBusVehicleReceiver
import com.geeksville.mavlink.MavlinkReceiver
import com.geeksville.apiproxy.LiveUploader
import com.geeksville.apiproxy.APIProxyActor

object Main extends Logging {

  val arduPilotId = 1
  val groundControlId = 255
  val wingmanId = Wingman.systemId
  def system = MockAkka.system

  /**
   * We use a systemId 2, because the ardupilot is normally on 1.
   */
  val systemId: Int = 2

  def createMavlinkClient(mkStream: () => MavlinkStreamReceiver) {
    try {
      // val mavSerial = Akka.actorOf(Props(MavlinkPosix.openSerial(port, baudRate)), "serrx")
      val mavSerial = system.actorOf(Props(mkStream()), "serrx")

      // Anything coming from the controller app, forward it to the serial port
      MavlinkEventBus.subscribe(mavSerial, groundControlId)

      // Also send anything from our active agent to the serial port
      MavlinkEventBus.subscribe(mavSerial, VehicleSimulator.andropilotId)

      // Anything from the wingman, send it to the serial port
      MavlinkEventBus.subscribe(mavSerial, wingmanId)
    } catch {
      case ex: NoSuchPortException =>
        logger.error("No serial port found, disabling...")
    }
  }

  def createSerial() {
    try {
      // First try to find an FTDI device (using libftdi) - if that throws then fall back to rxtx 
      def serDriver() = try {
        MavlinkPosix.openFtdi(null, 57600)
      } catch {
        case ex: Exception =>
          MavlinkPosix.openSerial("/dev/ttyACM0", 115200)
      }

      createMavlinkClient(serDriver)
    } catch {
      case ex: NoSuchPortException =>
        logger.error("No serial port found, disabling...")
    }
  }

  /**
   * A mavlink streamer that just reads packets from built-in test data
   */
  def createTestTlogInput() {
    createMavlinkClient { () =>
      val s = new BufferedInputStream(getClass.getResourceAsStream("test.tlog"), 8192)
      TlogStreamReceiver.open(s)
    }
  }

  def createSITLClient() = createMavlinkClient(() => MavlinkTCP.connect("localhost", 5760))

  /**
   * A quick hack to send a bunch of traffic in one direction and time RC overrides the other way
   */
  def testRadios() {

    logger.info("Starting radio test")
    val serGcs = system.actorOf(Props(MavlinkPosix.openFtdi("A900X44V", 57600)), "serrx0")
    val serVehicle = system.actorOf(Props(MavlinkPosix.openFtdi("A1011M1P", 57600)), "serrx1")

    //MavlinkEventBus.subscribe(serGcs, 1) // Anything from vehicle goes to gcs
    //MavlinkEventBus.subscribe(serVehicle, 253) // Anything from gcs goes to vehicle

    val rcrcv = system.actorOf(Props(new RCOverrideDebug(253)), "gclog")
    //MockAkka.actorOf(new LogIncomingMavlink(1), "vlog")
    //MockAkka.actorOf(new LogIncomingMavlink(253), "gclogdump")

    val gcs = system.actorOf(Props {
      val a = new DirectSending(253)
      a.sendingInterface = Some(serGcs)
      a
    }, "fakegcs")

    val vehicle = system.actorOf(Props {
      val a = new StressTestVehicle(1)
      a.sendingInterface = Some(serVehicle)
      a
    }, "fakevehicle")

    throw new Exception("rc test not yet updated for akka")
    // Send some fake RC overrides
    /*
    system.scheduler.schedule(2 seconds, 1000 milliseconds) { () =>
      val rc = gcs.rcChannelsOverride(1)
      val now = System.currentTimeMillis
      rc.chan1_raw = (now.toInt & 0xffff)
      rc.chan2_raw = ((now >> 16).toInt & 0xffff)
      rc.chan3_raw = ((now >> 32).toInt & 0xffff)
      rc.chan4_raw = ((now >> 48).toInt & 0xffff)

      //logger.debug(s"Sending override at $now")
      rcrcv.expectedSend = now
      serVehicle ! rc // Send to the interface directly rather than using mavlinkSend - because we want to control which port gets what data
    }
*/
  }

  def main(args: Array[String]) {
    logger.info("FlightLead running...")
    logger.debug("CWD is " + System.getProperty("user.dir"))

    // Needed for rxtx native code
    //val libprop = "java.library.path"
    System.setProperty("gnu.io.rxtx.SerialPorts", "/dev/ttyACM0:/dev/ttyUSB0")

    val arch = System.getProperty("os.arch")
    logger.info("arch: " + arch)
    SystemTools.addDir("libsrc/" + arch) // FIXME - skanky hack to find rxtx dll

    // We don't want anyone else's native libraries
    System.setProperty("jna.nosys", "true")

    // FIXME - select these options based on cmd line flags
    val startOutgoingUDP = false
    val startIncomingUDP = true
    val startSerial = false
    val startSITL = false
    val startFlightLead = false
    val startWingman = false
    val startMonitor = true
    val startMavServe = true
    val startSimData = false
    val dumpSerialRx = false
    val logToConsole = false
    val logToFile = false
    val startRadios = false
    val liveUpload = true

    if (startSerial)
      createSerial()

    if (startSimData)
      createTestTlogInput()

    if (startRadios)
      testRadios()

    if (startSITL)
      createSITLClient()

    if (liveUpload)
      LiveUploader.create(system, APIProxyActor.testAccount, true)

    if (startFlightLead) {
      // Create flightlead actors
      // If you want logging uncomment the following line
      // Akka.actorOf(Props(new LogIncomingMavlink(VehicleSimulator.systemId)), "hglog")
      system.actorOf(Props(new FlightLead), "lead")
      system.actorOf(Props(new IGCPublisher(getClass.getResourceAsStream("pretty-good-res-dumps-1hr.igc"))), "igcpub")

      // Watch for failures
      class FullMonitor extends HeartbeatMonitor
      MavlinkEventBus.subscribe(system.actorOf(Props { new FullMonitor }), systemId)
    }

    //
    // Wire up our subscribers
    //

    if (startOutgoingUDP || startIncomingUDP) {
      // FIXME create this somewhere else
      logger.info("Opening UDP link")

      val mavUDP = system.actorOf(Props(if (startOutgoingUDP)
        new MavlinkUDP(destHostName = Some("192.168.0.160"), destPortNumber = Some(MavlinkUDP.portNumber))
      else
        new MavlinkUDP(localPortNumber = Some(MavlinkUDP.portNumber))), "mavudp")

      // We don't want to send stuff from sysId one to this port (creates a loop when it arrived at that port)
      // MavlinkEventBus.subscribe(mavUDP, arduPilotId)

      // Anything coming from the controller app, forward it to the serial port
      MavlinkEventBus.subscribe(mavUDP, groundControlId)

      // Also send anything from our active agent to the serial port
      MavlinkEventBus.subscribe(mavUDP, VehicleSimulator.andropilotId)

      // Also send our wingman and flightlead planes to the ground control app
      MavlinkEventBus.subscribe(mavUDP, wingmanId)
      MavlinkEventBus.subscribe(mavUDP, systemId)
    }

    if (startMonitor) {
      // Keep a complete model of the arduplane state
      var vModel: VehicleModel = null
      val vehicle = system.actorOf(Props {
        vModel = new VehicleModel with EventBusVehicleReceiver with MavlinkReceiver
        vModel.listenOnly = startSimData // If using sim data, don't try talking with it
        vModel
      })

      if (startMavServe) {
        val gcs = new TempGCSModel(vModel)
        val adapter = new GCSAdapter(gcs)
        system.actorOf(Props(new PosixWebserver(adapter)))
      }
    }

    // Anything from our sim lead, send it to the controller app (so it will hopefully show him)
    // Doesn't work yet - mission planner freaks out
    // MavlinkEventBus.subscribe(mavUDP, VehicleSimulator.systemId)

    if (startWingman)
      // Create wingman actors
      system.actorOf(Props(new Wingman), "wing")

    if (logToConsole) {
      // Include this if you want to see all traffic from the ardupilot (use filters to keep less verbose)
      system.actorOf(Props(new LogIncomingMavlink(arduPilotId,
        if (dumpSerialRx)
          LogIncomingMavlink.allowDefault
        else
          LogIncomingMavlink.allowNothing)), "ardlog")

      // to see GroundControl packets
      system.actorOf(Props(new LogIncomingMavlink(groundControlId)), "gclog")
    }

    if (logToFile) {
      // Generate log files mission control would understand
      val logger = system.actorOf(Props(LogBinaryMavlink.create(true)), "gclog")
      MavlinkEventBus.subscribe(logger, arduPilotId)
      MavlinkEventBus.subscribe(logger, groundControlId)
      MavlinkEventBus.subscribe(logger, VehicleSimulator.andropilotId)
    }

    def shell = new ScalaShell() {
      override def name = "flight"
      override def initCmds = Seq("import com.geeksville.flight.lead.ShellCommands._")
    }
    shell.run()

    logger.info("Shutting down actors...")
    system.shutdown()
  }
}
