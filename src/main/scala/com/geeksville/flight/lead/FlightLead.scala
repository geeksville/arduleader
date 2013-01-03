package com.geeksville.flight.lead

// Standard akka imports
import akka.actor._
import akka.util.Duration
import akka.util.duration._
import com.geeksville.mavlink._
import com.geeksville.flight._
import com.geeksville.akka.InstrumentedActor
import com.geeksville.flight.wingman.Wingman
import com.geeksville.util.Counted
import com.geeksville.util.SystemTools
import org.mavlink.messages.MAVLinkMessage
import com.geeksville.shell.ScalaShell
import com.geeksville.shell.ScalaConsole

/**
 * Listen for GPS Locations on the event bus, and drive our simulated vehicle
 */
class FlightLead extends InstrumentedActor with VehicleSimulator {

  private val throttle = new Counted(10)

  def sendMavlink(m: MAVLinkMessage) = MavlinkEventBus.publish(m)

  // Send a heartbeat every 10 seconds 
  context.system.scheduler.schedule(0 milliseconds, 1 seconds) {
    sendMavlink(heartbeat)
  }

  context.system.eventStream.subscribe(self, classOf[Location])

  def receive = {
    case l: Location =>
      sendMavlink(makePosition(l.lat, l.lon, l.alt))
      sendMavlink(makeGPSRaw(l.lat, l.lon, l.alt))
      throttle { i =>
        val msg = "Emitted %d points...".format(i)
        log.info(msg)
        sendMavlink(makeStatusText(msg))

        sendMavlink(makeSysStatus())
      }
  }
}

object FlightLead {

  def main(args: Array[String]) {
    println("FlightLead running...")
    println("CWD is " + System.getProperty("user.dir"))

    // Needed for rxtx native code
    //val libprop = "java.library.path"
    System.setProperty("gnu.io.rxtx.SerialPorts", "/dev/ttyACM0:/dev/ttyUSB0")
    SystemTools.addDir("libsrc") // FIXME - skanky hack to find rxtx dll

    //val mavSerial = Akka.actorOf(Props(new MavlinkSerial("/dev/ttyACM0")), "serrx")

    // FIXME create this somewhere else
    val mavUDP = Akka.actorOf(Props[MavlinkUDP], "mavudp")

    // Create flightlead actors
    // If you want logging uncomment the following line
    // Akka.actorOf(Props(new LogIncomingMavlink(VehicleSimulator.systemId)), "hglog")
    Akka.actorOf(Props[FlightLead], "lead")
    Akka.actorOf(Props(new IGCPublisher("testdata/pretty-good-res-dumps-1hr.igc")), "igcpub")

    //
    // Wire up our subscribers
    //

    val arduPilotId = Wingman.targetSystemId
    val groundControlId = 255

    // Anything coming from the controller app, forward it to the serial port
    // MavlinkEventBus.subscribe(mavSerial, groundControlId)

    // Anything from the ardupilot, forward it to the controller app
    MavlinkEventBus.subscribe(mavUDP, arduPilotId)

    // Anything from our sim lead, send it to the controller app (so it will hopefully show him)
    // Doesn't work yet - mission planner freaks out
    // MavlinkEventBus.subscribe(mavUDP, VehicleSimulator.systemId)

    // Create wingman actors

    Akka.actorOf(Props[Wingman], "wing")

    // Include this if you want to see all traffic from the ardupilot (use filters to keep less verbose)
    Akka.actorOf(Props(new LogIncomingMavlink(arduPilotId, LogIncomingMavlink.allowNothing)), "ardlog")

    // to see GroundControl packets
    Akka.actorOf(Props(new LogIncomingMavlink(groundControlId)), "gclog")

    val shell = new ScalaConsole()
    shell.run()
  }
}