import java.util.HashMap;
import java.util.Random;

public class LanguageModel {

    // The map of this model.
    // Maps windows to lists of charachter data objects.
    HashMap<String, List> CharDataMap;
    
    // The window length used in this model.
    int windowLength;
    
    // The random number generator used by this model. 
	private Random randomGenerator;

    /** Constructs a language model with the given window length and a given
     *  seed value. Generating texts from this model multiple times with the 
     *  same seed value will produce the same random texts. Good for debugging. */
    public LanguageModel(int windowLength, int seed) {
        this.windowLength = windowLength;
        randomGenerator = new Random(seed);
        CharDataMap = new HashMap<String, List>();
    }

    /** Constructs a language model with the given window length.
     * Generating texts from this model multiple times will produce
     * different random texts. Good for production. */
    public LanguageModel(int windowLength) {
        this.windowLength = windowLength;
        randomGenerator = new Random();
        CharDataMap = new HashMap<String, List>();
    }

    /** Builds a language model from the text in the given file (the corpus). */
	public void train(String fileName) {
		In in = new In(fileName);
		String text = in.readAll();
		if (text != null) {
			text = text.replace("\r", "");
		}
		if (text == null) return;
		int n = text.length();
		for (int i = 0; i + windowLength < n; i++) {
			String window = text.substring(i, i + windowLength);
			char next = text.charAt(i + windowLength);
			List list = CharDataMap.get(window);
			if (list == null) {
				list = new List();
				CharDataMap.put(window, list);
			}
			list.update(next);
		}
		for (String key : CharDataMap.keySet()) {
			calculateProbabilities(CharDataMap.get(key));
		}
	}

    // Computes and sets the probabilities (p and cp fields) of all the
	// characters in the given list. */
	void calculateProbabilities(List probs) {				
		int total = 0;
		ListIterator it = probs.listIterator(0);
		while (it != null && it.hasNext()) {
			CharData cd = it.next();
			total += cd.count;
		}
		if (total == 0) return;
		it = probs.listIterator(0);
		double cum = 0.0;
		while (it != null && it.hasNext()) {
			CharData cd = it.next();
			cd.p = ((double) cd.count) / total;
			cum += cd.p;
			cd.cp = cum;
		}
	}

    // Returns a random character from the given probabilities list.
	char getRandomChar(List probs) {
		if (probs == null || probs.getSize() == 0) return ' ';
		double r = randomGenerator.nextDouble();
		ListIterator it = probs.listIterator(0);
		char last = ' ';
		while (it != null && it.hasNext()) {
			CharData cd = it.next();
			last = cd.chr;
			if (r < cd.cp) return cd.chr;
		}
		return last;
	}

    /**
	 * Generates a random text, based on the probabilities that were learned during training. 
	 * @param initialText - text to start with. If initialText's last substring of size numberOfLetters
	 * doesn't appear as a key in Map, we generate no text and return only the initial text. 
	 * @param numberOfLetters - the size of text to generate
	 * @return the generated text
	 */
	public String generate(String initialText, int textLength) {
		if (initialText == null) return "";
		if (initialText.length() < windowLength) return initialText;
		StringBuilder sb = new StringBuilder();
		sb.append(initialText);
		String window = initialText.substring(initialText.length() - windowLength);
		for (int i = 0; i < textLength; i++) {
			List list = CharDataMap.get(window);
			if (list == null) break;
			char next = getRandomChar(list);
			sb.append(next);
			window = window.substring(1) + next;
		}
        return sb.toString();
	}

    /** Returns a string representing the map of this language model. */
	public String toString() {
		StringBuilder str = new StringBuilder();
		for (String key : CharDataMap.keySet()) {
			List keyProbs = CharDataMap.get(key);
			str.append(key + " : " + keyProbs + "\n");
		}
		return str.toString();
	}
}
