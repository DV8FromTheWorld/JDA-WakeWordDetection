/*
 * Copyright 2018 Picovoice Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ai.picovoice.porcupine;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * Instrumented test, for Porcupine library.
 */
public class PorcupineTest {

    private final static String[] KEYWORD_FILENAMES = {
            "alexa.ppn", "americano.ppn", "avocado.ppn", "blueberry.ppn", "bumblebee.ppn",
            "caterpillar.ppn", "christina.ppn", "dragonfly.ppn", "flamingo.ppn", "francesca.ppn",
            "grapefruit.ppn", "grasshopper.ppn", "iguana.ppn", "picovoice.ppn", "pineapple.ppn",
            "porcupine.ppn", "raspberry.ppn", "terminator.ppn", "vancouver.ppn"
    };

    /**
     * Copy model parameters, test WAV files, and porcupine keyword files to the internal storage.
     * @throws IOException if there is an error while reading/writing files.
     */
//    public static void copyFiles() throws IOException {
//        copyFileToInternalStorage("params.pv");
//
//        copyFileToInternalStorage("porcupine.wav");
//        copyFileToInternalStorage("multiple_keywords.wav");
//
//        for (String keyword_filename: KEYWORD_FILENAMES) {
//            copyFileToInternalStorage(keyword_filename);
//        }
//    }

    public static void main(String[] args) throws IOException, PorcupineException {
        new PorcupineTest().testProcessFrame();
    }

    /**
     * Read a test WAV file containing one utterance of keyword 'porcupine', process it using
     * Porcupine library, and assure that it triggers once and only once.
     * @throws PorcupineException if there is an error while processing the raw PCM data in the
     * Porcupine library.
     * @throws IOException if there is an error while reading the WAV file.
     */
    public void testProcessFrame() throws PorcupineException, IOException {
        final String modelFilePath = getFilePath("C:\\Users\\Austin\\Desktop\\VoiceBot\\Runtime\\porcupine_params.pv");
        final String keywordFilePath = getFilePath("Hey_Yui_windows.ppn");
        System.out.println(modelFilePath);
        Porcupine porcupine = new Porcupine(modelFilePath, keywordFilePath, 0.7f);

        final int frameLength = porcupine.getFrameLength();
        File audioFile = new File("16bit_16khz_mono.wav");

        WavReader wavReader = null;
        short[] shortBuffer = new short[frameLength];
        int count = 0;
        try {
            wavReader = new WavReader(audioFile);
            while (wavReader.readFrames(shortBuffer, frameLength) == frameLength) {
                if (porcupine.processFrame(shortBuffer)) {
                    count += 1;
                }
            }
        } finally {
            if (wavReader != null) {
                wavReader.close();
            }
        }

        System.out.println(count);
        porcupine.delete();
    }

    private static final Integer[] EXPECTED_KEYWORD_INDICES = {
            15, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18
    };

    /**
     * Read a test WAV test file containing multiple utterances of different keywords, process it
     * using Porcupine library, and make sure it detects them correctly.
     * @throws PorcupineException if there is an error while processing the raw PCM data in the
     * Porcupine library.
     * @throws IOException if there is an error while reading the WAV file.
     */
    public void testProcessFrameMultipleKeywords() throws PorcupineException, IOException {
        final String modelFilePath = getFilePath("params.pv");

        String[] keyword_file_paths = new String[KEYWORD_FILENAMES.length];
        for (int i = 0; i < KEYWORD_FILENAMES.length; i++) {
            keyword_file_paths[i] = getFilePath(KEYWORD_FILENAMES[i]);
        }

        float[] sensitivities = new float[KEYWORD_FILENAMES.length];
        for (int i = 0; i < KEYWORD_FILENAMES.length; i++) {
            sensitivities[i] = 1;
        }

        Porcupine porcupine = new Porcupine(modelFilePath, keyword_file_paths, sensitivities);

        final int frameLength = porcupine.getFrameLength();
        File audioFile = new File("multiple_keywords.wav");

        WavReader wavReader = null;
        short[] shortBuffer = new short[frameLength];
        ArrayList<Integer> keyword_indices = new ArrayList<>();
        try {
            wavReader = new WavReader(audioFile);
            while (wavReader.readFrames(shortBuffer, frameLength) == frameLength) {
                final int keyword_index = porcupine.processFrameMultipleKeywords(shortBuffer);
                if (keyword_index >= 0) {
                    keyword_indices.add(keyword_index);
                }
            }
        } finally {
            if (wavReader != null) {
                wavReader.close();
            }
        }


        porcupine.delete();
    }

    private static String getFilePath(String name) {
        return new File(name).getAbsolutePath();
    }

//    /**
//     * Copy a file from the resources directory to internal storage.
//     * @param filename The name of the file.
//     * @throws IOException if there is an error in the IO operations.
//     */
//    private static void copyFileToInternalStorage(String filename) throws IOException {
//        InputStream is = null;
//        OutputStream os = null;
//        try {
//            ClassLoader classLoader = PorcupineTest.class.getClassLoader();
//            is = new BufferedInputStream(classLoader.getResourceAsStream(filename), 256);
//            os = new BufferedOutputStream(CONTEXT.openFileOutput(filename, Context.MODE_PRIVATE),
//                    256);
//            int r;
//            while ((r = is.read()) != -1) {
//                os.write(r);
//            }
//            os.flush();
//        } finally {
//            if (is != null) {
//                is.close();
//            }
//            if (os != null) {
//                os.close();
//            }
//        }
//    }
}