package net.notfab.voicebot;

public class Start {
    public static void main(String[] args) throws Exception {
        /*Configuration configuration = new Configuration();

        configuration.setAcousticModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us");
        configuration.setDictionaryPath("resource:/edu/cmu/sphinx/models/en-us/cmudict-en-us.dict");
        configuration.setLanguageModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us.lm.bin");

        Transcoder.transcode("Sphinx/Recording.wav", "Sphinx/Mono.wav", DefaultAttributes.WAV_PCM_S16LE_MONO_8KHZ);

        StreamSpeechRecognizer recognizer = new StreamSpeechRecognizer(configuration);
        InputStream stream = new FileInputStream(new File("Sphinx/Mono.wav"));

        recognizer.startRecognition(stream);
        SpeechResult result;
        while ((result = recognizer.getResult()) != null) {
            System.out.format("Hypothesis: %s\n", result.getHypothesis());
        }
        recognizer.stopRecognition();*/
        //        new VoiceBot();
        new VoiceReceiveHandler();
    }

}