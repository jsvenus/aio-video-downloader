package tts;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.util.ArrayList;
import java.util.Locale;

/**
 * TTS : text to speech.
 * Created by shibaprasad on 2/3/2015.
 */
public class TTS {

    /**
     * The private TTS Engine.
     */
    public TextToSpeech textToSpeech;
    public Thread thread;
    private int queueNumber = 0;
    private Context context;

    /**
     * Public constructor for the TTS.
     *
     * @param context the service context.
     */
    public TTS(final Context context) {
        this.context = context;

        textToSpeech = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                //do nothing
            }
        });

    }

    /**
     * Speak the text.
     *
     * @param textInput the text to be spoken.
     */
    @SuppressWarnings("ConstantConditions")
    public void speakRun(String textInput) {
        if (this.textToSpeech != null) {
            if (textInput.length() <= 200) {
                int result = this.textToSpeech.speak(textInput, TextToSpeech.QUEUE_FLUSH, null);
                if (result == TextToSpeech.SUCCESS)
                    this.queueNumber++;
                Log.i(getClass().getName(), "QUEUE NUMBER ADDING.. : " + queueNumber);
            } else {
                String tmpText = textInput;
                while (tmpText != null && tmpText.length() > 1) {
                    String readingText = tmpText.substring(0, tmpText.indexOf('.') + 1);

                    if (readingText == null || readingText.length() < 1)
                        readingText = tmpText.substring(0, (tmpText.length() > 200 ?
                                200 : tmpText.length()));
                    int result = this.textToSpeech.speak(readingText, TextToSpeech.QUEUE_ADD, null);
                    if (result == TextToSpeech.SUCCESS)
                        this.queueNumber++;
                    Log.i(getClass().getName(), "QUEUE NUMBER ADDING.. : " + queueNumber + "\n" +
                            readingText);

                    try {
                        String x = tmpText.substring(readingText.length());
                        if (x.length() > 1) {
                            tmpText = x;
                        } else {
                            tmpText = null;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        tmpText = null;
                    }
                }
            }
        }
    }

    public void speak(String text) {
        thread = getSpeakThread(text);
        thread.start();
    }

    private Thread getSpeakThread(final String text) {
        return new Thread(new Runnable() {
            @Override
            public void run() {
                speakRun(text);
            }
        });
    }

    public void stop() {
        try {
            if (textToSpeech.isSpeaking()) {
                while (this.queueNumber > 0) {
                    textToSpeech.stop();
                    queueNumber--;
                    Log.i(getClass().getName(), "QUEUE NUMBER : " + queueNumber);
                }
            } else {
                this.queueNumber = 0;
                Log.i(getClass().getName(), "QUEUE NUMBER : " + queueNumber);
            }


        } catch (Exception error) {
            error.printStackTrace();
        }
    }


    /**
     * Get the support languages by the text-to-speech engine.
     *
     * @return the array of supported locales.
     */
    public ArrayList<Locale> getSupportedLanguage() {
        ArrayList<Locale> localeArrayList = new ArrayList<Locale>();

        Locale[] allLocales = Locale.getAvailableLocales();
        for (Locale locale : allLocales) {
            try {
                int responseCode = textToSpeech.isLanguageAvailable(locale);

                boolean hasVariant = (null != locale.getVariant() && locale.getVariant().length() > 0);
                boolean hasCountry = (null != locale.getCountry() && locale.getCountry().length() > 0);

                boolean isLocaleSupported =
                        !hasVariant && !hasCountry && responseCode == TextToSpeech.LANG_AVAILABLE ||
                                !hasVariant && hasCountry && responseCode == TextToSpeech.LANG_COUNTRY_AVAILABLE ||
                                responseCode == TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE;

                if (isLocaleSupported) {
                    localeArrayList.add(locale);
                }
            } catch (Exception ignored) {
            }
        }
        return localeArrayList;
    }
}
