SerialTest
==========

This is Serial Testing for CMUCam4.

Currently not working that well, using rxtx. Will re-implement USBSerialConnection using another library soon.


For manual testing using minicom, follow the following steps:
- Start minicom with `minicom`.
- Set echo on with `^A` then `Z` then `E`
- Set `add line feed` enabled with `^A` then `Z` then `A`
- Reset the CMUCam4 with `RS` then pressing enter.
