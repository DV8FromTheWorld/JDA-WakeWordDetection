package net.notfab.voicebot;

import net.dv8tion.ServiceUtil;
import net.dv8tion.jda.core.utils.IOUtil;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class NewStart {
    public static void main(String[] args) throws IOException, UnsupportedAudioFileException {
        System.out.println(Character.MAX_VALUE + Character.MAX_VALUE);
        System.out.println(Character.MAX_VALUE + 0);
//        System.out.println(a);
//        System.out.println(Short.toUnsignedInt(a));
//        System.out.println(Integer.toHexString(a));
//        a += 1;
//        System.out.println(Short.toUnsignedInt(a));
//        System.out.println(Integer.toHexString(a));
//        new VoiceReceiveHandler();
//        ServiceUtil.loadServices();
//        final File file = new File("16k_48k.wav");
//        AudioInputStream audioFile = AudioSystem.getAudioInputStream(file);
//        AudioInputStream convertedAudio = toPorcupineAudioStream(audioFile);
//
//
////         do something with the resampled audio data
////        AudioSystem.write(resampledStream, AudioFileFormat.Type.WAVE, new File("16k_16k.wav"));
//        AudioSystem.write(convertedAudio, AudioFileFormat.Type.WAVE, new File("16bit_16khz_mono.wav"));
    }

    public static AudioInputStream toPorcupineAudioStream(AudioInputStream sourceStream) throws IOException {
        final AudioFormat sourceFormat = sourceStream.getFormat();
        final AudioFormat to16khzFormat = new AudioFormat(
                sourceFormat.getEncoding(),
                16000f, // target sample rate
                sourceFormat.getSampleSizeInBits(),
                sourceFormat.getChannels(),
                sourceFormat.getFrameSize(),
                512f, // target frame rate
                sourceFormat.isBigEndian()
        );
        final AudioInputStream resampled16khz = AudioSystem.getAudioInputStream(to16khzFormat, sourceStream);


        final AudioFormat toMonoLEFormat = new AudioFormat(
                to16khzFormat.getEncoding(),
                to16khzFormat.getSampleRate(),
                to16khzFormat.getSampleSizeInBits(),
                1,
                to16khzFormat.getFrameSize(),
                to16khzFormat.getFrameRate(),
                false
        );

//        byte[] arr = IOUtil.readFully(AudioSystem.getAudioInputStream(toMonoLEFormat, resampled16khz));
//        char[] shorts = new char[arr.length / 2];
//        char[] converted = new char[arr.length / 4];
//
//        ByteBuffer.wrap(arr).order(ByteOrder.LITTLE_ENDIAN).asCharBuffer().get(shorts);
//
//        for (int i = 0; i < shorts.length; i++) {
//            char a = shorts[i*2];
//            char b = shorts[i*2 + 1];
//
//            char sum = (char) Math.max(a + b, Character.MAX_VALUE);
//            converted[i] = sum;
//        }
//
//        byte[] convertedArr = new byte[converted.length * 2];
//        ByteBuffer.wrap(convertedArr).order(ByteOrder.LITTLE_ENDIAN).asCharBuffer().put(converted);

//        return new AudioInputStream(new ByteArrayInputStream(convertedArr), toMonoLEFormat, convertedArr.length);
        return AudioSystem.getAudioInputStream(toMonoLEFormat, resampled16khz);
    }
}
