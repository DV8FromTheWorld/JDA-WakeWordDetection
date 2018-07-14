package net.notfab.voicebot;

import ai.picovoice.porcupine.Porcupine;
import ai.picovoice.porcupine.PorcupineException;
import net.dv8tion.jda.core.audio.AudioReceiveHandler;
import net.dv8tion.jda.core.audio.CombinedAudio;
import net.dv8tion.jda.core.audio.UserAudio;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.utils.IOUtil;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class VoiceReceiveHandler implements AudioReceiveHandler {

    final String modelFilePath = "C:\\Users\\Austin\\Desktop\\VoiceBot\\Runtime\\porcupine_params.pv"; // It is available at lib/common/porcupine_params.pv
    final String keywordFilePath = "C:\\Users\\Austin\\Desktop\\VoiceBot\\porcupine.ppn";
//final String keywordFilePath = "C:\\Users\\Austin\\Desktop\\VoiceBot\\Hey_Yui_windows.ppn";
//    final String keywordFilePath = "C:\\Users\\Austin\\Desktop\\VoiceBot\\Austin_windows.ppn";
    final float sensitivity = 0.5f;
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

    int found = 0;
    int notFound = 0;
    private void setupActiveListener() {
        Thread t = new Thread(() -> {
            AudioInputStream currentStream = null;
            int currentBytes = 0;
            byte[] bBuffer = new byte[porcupine.getFrameLength() * 2];
            short[] sBuffer = new short[porcupine.getFrameLength()];

//            try {
//                byte[] buffer = new byte[3840 * MAX_PACKETS];
//                AudioInputStream stream = AudioSystem.getAudioInputStream(new File("porcupine.wav"));
//                AudioInputStream stream2 = AudioSystem.getAudioInputStream(new File("16bit_16khz_mono.wav"));
//                System.out.println(stream.getFormat());
//                System.out.println(stream2.getFormat());
//                int read = 0;
//                while ((read = stream.read(buffer)) != -1) {
//                    convertedAudio.add(new AudioInputStream(new ByteArrayInputStream(buffer), stream.getFormat(), buffer.length));
//                }

//                convertedAudio.add(stream);
//                convertedAudio.add(stream2);
//                System.out.println("added");
//            } catch (UnsupportedAudioFileException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }

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
            //TODO: replace with System.arraycopy
            System.arraycopy(userAudioBytes, 0, buffer, (currentBuffer * 3840), 3840);
//            System.arraycopy(userAudioBytes, 0, bb,     (currentCC     * 3840), 3840);
//            for (int i = 0; i < 3840; i++) {
//                buffer[i + (3840 * currentBuffer)] = userAudioBytes[i];
////                audio.add(userAudioBytes[i]);
////                bb[i + (3840 * currentCC)] = userAudioBytes[i];
//            }

//            currentCC++;
//            if (currentCC >= 99) {
//                try {
//                    System.out.println("Making");
//                    AudioInputStream stream = new AudioInputStream(new ByteArrayInputStream(bb), AudioReceiveHandler.OUTPUT_FORMAT, bb.length);
//                    System.out.println("Converting");
//                    AudioSystem.write(stream, AudioFileFormat.Type.WAVE, new File("16bit_16khz_mono.wav"));
//                    System.out.println("Saved");
//                    currentCC = 0;
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                audio.clear();
//            }

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

//            for (byte b : userAudioBytes) {
//                audio.add(b);
//            }
//            times++;
//
//            if (times >= 300) {
//                byte[] bAudio = new byte[audio.size()];
//                for (int i = 0; i < audio.size(); i++) {
//                    bAudio[i] = audio.get(i);
//                }
//                AudioInputStream stream = new AudioInputStream(new ByteArrayInputStream(bAudio), AudioReceiveHandler.OUTPUT_FORMAT, bAudio.length / 4);
////                try {
////                    AudioSystem.write(NewStart.toPorcupineAudioStream(stream), AudioFileFormat.Type.WAVE, new File("16bit_16khz_mono.wav"));
////                } catch (IOException e) {
////                    e.printStackTrace();
////                }
//                try {
//                    byte[] bBuffer = IOUtil.readFully(NewStart.toPorcupineAudioStream(stream));
//                    short[] sBuffer = new short[bBuffer.length / 2];
//                    ByteBuffer.wrap(bBuffer).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(sBuffer);
//                    System.out.println(audio.size() + " : " + bBuffer.length + " : " + sBuffer.length);
//
//                    short[] buf = new short[512];
//                    int i = 0;
//                    int found = 0;
//                    int notFound = 0;
//                    for (short s : sBuffer) {
//                        buf[i] = s;
//
//                        i++;
//                        if (i >= 512) {
//                            i = 0;
//                            if (porcupine.processFrame(buf)) {
//                                found++;
//                            }
//                            else {
//                               notFound++;
//                            }
//                        }
//                    }
//
//                    System.out.println("Found: " + (found == 0 ? "0" : "YESSSSSSSS (" + found + ")"));
//                    System.out.println("NotFound: " + notFound);
//                    System.out.println("=========");
//                } catch (IOException e) {
//                    e.printStackTrace();
//                } catch (PorcupineException e) {
//                    e.printStackTrace();
//                }
//
//                times = 0;
//                audio.clear();
//            }
        }
//        byte[] audio = convert(userAudio.getAudioData(1));
//        if(audio == null) {
//            System.out.println("Failed conversion");
//            return;
//        }
//        try {
//            Boolean b = porcupine.processFrame(toShort(audio));
//            if(b) {
//                System.out.println("Yay");
//            } else {
//                System.out.println("nay");
//            }
//        } catch (PorcupineException e) {
//            e.printStackTrace();
//        }
    }

    private byte[] convert(byte[] original) {
//        try {
//            AudioInputStream streamedAudioInputStream = Streamer.stream("https://cdn.notfab.net/Recording.wav", attributes);
//            return streamedAudioInputStream.readAllBytes();
//        } catch (EncoderException | IOException e) {
//            e.printStackTrace();
//            return null;
//        }
        return null;
    }

}