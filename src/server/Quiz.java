package server;

import java.util.ArrayList;
import java.util.List;

/**
 * class quiz
 * to prepare list of entries to test in main method of MyServer class
 */
public class Quiz {
    List<Entry> entries = new ArrayList<Entry>();

    public static Quiz createSampleQuiz() {
        Quiz q = new Quiz();
        Entry entry1 = new Entry("Question1 ....... ?", "good answer1!");
        entry1.addBadChoices("badChoiceTwo", "badChoiceThree", "badChoiceFour");
        Entry entry2 = new Entry("Question2 ....... ?", "good answer2!");
        entry2.addBadChoices("badChoiceTwo", "badChoiceThree", "badChoiceFour");
        Entry entry3 = new Entry("Question3 ....... ?", "good answer3!");
        entry3.addBadChoices("badChoiceTwo", "badChoiceThree", "badChoiceFour");
        Entry entry4 = new Entry("Question4 ....... ?", "good answer4!");
        entry4.addBadChoices("badChoiceTwo", "badChoiceThree", "badChoiceFour");
        Entry entry5 = new Entry("Question5 ....... ?", "good answer5!");
        entry5.addBadChoices("badChoiceTwo", "badChoiceThree", "badChoiceFour");
        Entry entry6 = new Entry("Question6 ....... ?", "good answer6!");
        entry6.addBadChoices("badChoiceTwo", "badChoiceThree", "badChoiceFour");
        Entry entry7 = new Entry("Question7 ....... ?", "good answer7!");
        entry7.addBadChoices("badChoiceTwo", "badChoiceThree", "badChoiceFour");
        Entry entry8 = new Entry("Question8 ....... ?", "good answer8!");
        entry8.addBadChoices("badChoiceTwo", "badChoiceThree", "badChoiceFour");
        Entry entry9 = new Entry("Question9 ....... ?", "good answer9!");
        entry9.addBadChoices("badChoiceTwo", "badChoiceThree", "badChoiceFour");
        Entry entry10 = new Entry("Question10 ....... ?", "good answer10!");
        entry10.addBadChoices("badChoiceTwo", "badChoiceThree", "badChoiceFour");

        q.entries.add(entry1);
        q.entries.add(entry2);
        q.entries.add(entry3);
        q.entries.add(entry4);
        q.entries.add(entry5);
        q.entries.add(entry6);
        q.entries.add(entry7);
        q.entries.add(entry8);
        q.entries.add(entry9);
        q.entries.add(entry10);

        return q;

    }

}
