package server;

class WordScrambler {

    private static final String[] words = {
            "abracadabra",
            "delphi",
            "dinosaur",
            "automaton",
            "python",
            "sleep",
            "thread",
            "posix",
            "windows",
            "ubuntu",
            "oracle",
            "variable"
    };

    private static final int wordsCount = words.length;

    private static String shuffleString(String string) {
        StringBuilder original = new StringBuilder(string);
        StringBuilder shuffled = new StringBuilder();

        while (original.length() != 0) {
            int randomPos = (int) (Math.random() * original.length());
            shuffled.append(original.charAt(randomPos));
            original.deleteCharAt(randomPos);
        }

        return shuffled.toString();
    }

    static String[] getRandomWord() {
        int randomPos = (int) (Math.random() * wordsCount);
        return new String[]{words[randomPos], shuffleString(words[randomPos])};
    }

}
