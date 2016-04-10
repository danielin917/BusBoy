package solarcar.vdcListener;

class UserBeat extends Thread {
    public static String beatSource;
    public UserBeat(String beatSource_in) {
        super("UserBeat");
        beatSource = beatSource_in;
    }

    @Override
    public void run() {
  //      int count = 0;
        while (true) {
            String hello = "beat id=" + beatSource + ": " + System.getProperty("user.name") + "'";
            VDCConn.get().sendMessage(hello);
			//VDCConn.get().sendMessage("cmd id='settrackingmode'");

            // indicate beat then wait 2 seconds
//            count = (count + 1) % 4;
//            	System.out.println("beat");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
            }
        }
    }
}