package net.notfab.voicebot;

import ai.picovoice.porcupine.Porcupine;
import ai.picovoice.porcupine.PorcupineException;
import net.dv8tion.jda.core.audio.AudioReceiveHandler;
import net.dv8tion.jda.core.audio.CombinedAudio;
import net.dv8tion.jda.core.audio.UserAudio;
import net.dv8tion.jda.core.entities.TextChannel;

import javax.sound.sampled.AudioInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.LinkedList;

public class VoiceReceiveHandler implements AudioReceiveHandler {

    final String modelFilePath = "C:\\Users\\Austin\\Desktop\\VoiceBot\\Runtime\\porcupine_params.pv"; // It is available at lib/common/porcupine_params.pv
    final String keywordFilePath = "C:\\Users\\Austin\\Desktop\\VoiceBot\\Runtime\\alexa_windows.ppn";
//    final String keywordFilePath = "C:\\Users\\Austin\\Desktop\\VoiceBot\\porcupine.ppn";
//final String keywordFilePath = "C:\\Users\\Austin\\Desktop\\VoiceBot\\Hey_Yui_windows.ppn";
//    final String keywordFilePath = "C:\\Users\\Austin\\Desktop\\VoiceBot\\Austin_windows.ppn";
    final float sensitivity = 1.0f;
    final TextChannel chan;

//    private Attributes attributes = DefaultAttributes.WAV_PCM_S16LE_MONO_44KHZ.getAttributes();

    Porcupine porcupine;

    public VoiceReceiveHandler(TextChannel chan) {
        this.chan = chan;
        try {
            porcupine = new Porcupine(modelFilePath, keywordFilePath, sensitivity);
            System.out.println(porcupine.getFrameLength());
            System.out.println(porcupine.getSampleRate());
            this.setupActiveListener();
        } catch (PorcupineException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean canReceiveCombined() {
        return false;
    }

    @Override
    public boolean canReceiveUser() {
        return true;
    }

    @Override
    public void handleCombinedAudio(CombinedAudio combinedAudio) {
        //System.out.println("CombinedAudio");
    }

    //https://hastebin.com/yafoqukibe.cs
    LinkedList<Byte> audio = new LinkedList<>();
    LinkedList<AudioInputStream> convertedAudio = new LinkedList<>();
    int times = 0;
    final int MAX_PACKETS = 10;
    int currentBuffer = 0;
    int currentCC = 0;
    byte[] bb = new byte[3840 * 2 * 50];
    byte[] buffer = new byte[3840 * MAX_PACKETS]; //200ms of data
    @Override
    public void handleUserAudio(UserAudio userAudio) {
        if (userAudio.getUser().getName().equals("DV8FromTheWorld")
            || userAudio.getUser().getName().equals("Yui")) {

            byte[] userAudioBytes = userAudio.getAudioData(1);
            System.arraycopy(userAudioBytes, 0, buffer, (currentBuffer * 3840), 3840);

            currentBuffer++;
            if (currentBuffer >= MAX_PACKETS) {
                AudioInputStream stream = new AudioInputStream(new ByteArrayInputStream(buffer), AudioReceiveHandler.OUTPUT_FORMAT, buffer.length);
                try {
                    System.out.println("Made new stream");
                    convertedAudio.push(NewStart.toPorcupineAudioStream(stream));
                } catch (IOException e) {
                    e.printStackTrace();
                }

                currentBuffer = 0;
            }
        }
    }

    int found = 0;
    int notFound = 0;
    private void setupActiveListener() {
        Thread t = new Thread(() -> {
            AudioInputStream currentStream = null;
            int currentBytes = 0;
            byte[] bBuffer = new byte[porcupine.getFrameLength() * 2];
            short[] sBuffer = new short[porcupine.getFrameLength()];

            try {
                while (true) {
                    if (currentStream == null && (currentStream = convertedAudio.poll()) == null) {
                        Thread.sleep(100);
                        continue;
                    }

                    int readBytes = currentStream.read(bBuffer, currentBytes, bBuffer.length - currentBytes);
                    if (readBytes == -1) {
                        System.out.println("Dropping spent AIS");
                        System.out.println("Found: " + found);
                        System.out.println("Not Found: " + notFound);
                        found = 0;
                        notFound = 0;
                        currentStream = null;
                        continue;
                    }

                    currentBytes += readBytes;
                    if (currentBytes == bBuffer.length) {
                        currentBytes = 0;
                        //                        System.out.println("Checking...");
                        ByteBuffer.wrap(bBuffer).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(sBuffer);
                        if (porcupine.processFrame(sBuffer)) {
                            found++;
                            chan.sendMessage("I heard the wakeword!").queue();
                            System.out.println("Found word! YEEEEEEEEEEEEEEEEEEEEEEES");
                        }
                        else {
                            notFound++;
                            //                            System.out.println("No Word.. :(");
                        }
                    }
                }
            } catch (IOException | InterruptedException | PorcupineException e) {
                e.printStackTrace();
            }
        });

        t.start();
    }

}