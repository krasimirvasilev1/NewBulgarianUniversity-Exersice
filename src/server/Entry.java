package server;

/**
 * a question with its choices and answer
 * question is in index zero
 * good answer is in index one
 * bad choices are in indexes 2,3,4
 */
public class Entry {
    private String[] data = new String[4];
    private int indexGoodAnswer = 0;
    private String question = "";

    //Constructor
    public Entry(String quest, String goodAnswer) {
        this.question = quest;
        data[0] = goodAnswer;
    }

    /**
     * method to add three bad choices
     * @param choiceTwo
     * @param choiceThree
     * @param choiceFour
     */
    public void addBadChoices(String choiceTwo, String choiceThree, String choiceFour) {
        data[1] = choiceTwo;
        data[2] = choiceThree;
        data[3] = choiceFour;
    }

    public String[] getData() {
        return data;
    }

    public void setData(String[] data) {
        this.data = data;
    }


    public String getQuestion() {
        return question;
    }

}
