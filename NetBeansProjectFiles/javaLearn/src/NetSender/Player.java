package NetSender;


import NetSender.TransmitterManager.SongListItem;
import java.io.File;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javazoom.jlgui.basicplayer.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Zero
 */
public class Player implements javazoom.jlgui.basicplayer.BasicPlayerListener{
    
    private enum PlayerState{PLAY,PAUSE}
    private PlayerState playerState;public boolean isPlaying(){return playerState==playerState.PLAY;}
    private String cachePath; public String getCachePath(){return cachePath;}
    private static Log log = LogFactory.getLog(Player.class);
    private Map audioInfo;
    private BasicPlayer theSoundPlayer;
    private SongListItem currentSong;public SongListItem getCurrentSongItem(){return currentSong;}
    public Player() {
            File cache=new File("Cache");
            if(!cache.exists())
                cache.mkdir();
            cachePath=cache.getAbsolutePath();
            theSoundPlayer = new BasicPlayer();
            theSoundPlayer.addBasicPlayerListener(this);
            playerState=PlayerState.PAUSE;
    }
    public void play(SongListItem item){
        try {
            theSoundPlayer.stop();
            theSoundPlayer.open(item.file);
            item.isPlaying=true;
            currentSong=item;
            theSoundPlayer.play();
            playerState=PlayerState.PLAY;
        } catch (BasicPlayerException ex) {
            Logger.getLogger(Player.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public void stop(){
        try {
            theSoundPlayer.stop();
            currentSong.isPlaying=false;
        } catch (BasicPlayerException ex) {
            Logger.getLogger(Player.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public void seek(double rate){
        if(playerState==PlayerState.PLAY)
            processSeek(rate);
    }
    public static void main(String[] args) {
        Player playerTest = new Player();
        //playerTest.play("Ëïéª - I Believe.mp3");
    }

    @Override
    public void opened(Object stream, Map properties) {
        //File curFile=(File)stream;
        audioInfo=properties;
    }

    long prevt=0;
    @Override
    public void progress(int bytesread, long microseconds, byte[] pcmdata, Map properties){
        long totalsec = (int) Math.round(getTimeLengthEstimation(audioInfo) / 1000);
        int byteslength = ((Integer) audioInfo.get("audio.length.bytes")).intValue();
        double  progress = bytesread * 1.0f / byteslength * 1.0f;
        long secondsAmount = (long)(totalsec * progress);
        long curt=System.currentTimeMillis();
        if(curt-prevt>500){
            prevt=curt;
            TransmitterManager.tellSongPos((int)secondsAmount,(int)totalsec,progress);
        }
    }

    @Override
    public void stateUpdated(BasicPlayerEvent event) {
        log.debug("Player:" + event + " (EDT=" + SwingUtilities.isEventDispatchThread() + ")");
        /*-- End Of Media reached --*/
        int state = event.getCode();
        Object obj = event.getDescription();
        if (state == BasicPlayerEvent.EOM)
        {
            if ((playerState == PlayerState.PAUSE) || (playerState == PlayerState.PLAY))
            {
                TransmitterManager.playNextSong();
            }
        }
        else if (state == BasicPlayerEvent.PLAYING)
        {
            //lastScrollTime = System.currentTimeMillis();
        }
        else if (state == BasicPlayerEvent.SEEKING)
        {
            //posValueJump = true;
        }
        else if (state == BasicPlayerEvent.SEEKED)
        {
            
        }
        else if (state == BasicPlayerEvent.OPENING)
        {
//            if ((obj instanceof URL) || (obj instanceof InputStream))
//            {
//                showTitle(ui.getResource("title.buffering"));
//            }
        }
        else if (state == BasicPlayerEvent.STOPPED)
        {
//            if (ui.getAcAnalyzer() != null)
//            {
//                ui.getAcAnalyzer().stopDSP();
//                ui.getAcAnalyzer().repaint();
//            }
        }
    }

    @Override
    public void setController(BasicController bc) {
        System.out.println("setController Not supported yet.");
    }
     /**
     * Process seek feature.
     * @param rate
     */
    protected void processSeek(double rate)
    {
        try
        {
            if ((audioInfo != null) && (audioInfo.containsKey("audio.type")))
            {
                String type = (String) audioInfo.get("audio.type");
                // Seek support for MP3.
                if ((type.equalsIgnoreCase("mp3")) && (audioInfo.containsKey("audio.length.bytes")))
                {
                    long skipBytes = (long) Math.round(((Integer) audioInfo.get("audio.length.bytes")).intValue() * rate);
                    log.debug("Seek value (MP3) : " + skipBytes);
                    theSoundPlayer.seek(skipBytes);
                }
            }
        }
        catch (BasicPlayerException ioe)
        {
            log.error("Cannot skip", ioe);
        }
    }
    public long getTimeLengthEstimation(Map properties)
    {
        long milliseconds = -1;
        int byteslength = -1;
        if (properties != null)
        {
            if (properties.containsKey("audio.length.bytes"))
            {
                byteslength = ((Integer) properties.get("audio.length.bytes")).intValue();
            }
            if (properties.containsKey("duration"))
            {
                milliseconds = (int) (((Long) properties.get("duration")).longValue()) / 1000;
            }
            else
            {
                // Try to compute duration
                int bitspersample = -1;
                int channels = -1;
                float samplerate = -1.0f;
                int framesize = -1;
                if (properties.containsKey("audio.samplesize.bits"))
                {
                    bitspersample = ((Integer) properties.get("audio.samplesize.bits")).intValue();
                }
                if (properties.containsKey("audio.channels"))
                {
                    channels = ((Integer) properties.get("audio.channels")).intValue();
                }
                if (properties.containsKey("audio.samplerate.hz"))
                {
                    samplerate = ((Float) properties.get("audio.samplerate.hz")).floatValue();
                }
                if (properties.containsKey("audio.framesize.bytes"))
                {
                    framesize = ((Integer) properties.get("audio.framesize.bytes")).intValue();
                }
                if (bitspersample > 0)
                {
                    milliseconds = (int) (1000.0f * byteslength / (samplerate * channels * (bitspersample / 8)));
                }
                else
                {
                    milliseconds = (int) (1000.0f * byteslength / (samplerate * framesize));
                }
            }
        }
        return milliseconds;
    }
}
