* Fix hang on exit when no chars coming in on serial port.  Cause is that we are in usb_read_bulk and closing
the port doesn't seem to cause that to fail.  Do I have to turn on timeouts (ugh)?

