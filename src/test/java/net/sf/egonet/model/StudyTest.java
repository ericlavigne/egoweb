package net.sf.egonet.model;

import java.util.List;
import java.util.Set;

import net.sf.egonet.EgonetService;
import net.sf.egonet.model.Answer.AnswerType;

public class StudyTest
{
	public void testUsage()
   	{
		EgonetService es = new EgonetService();

		Set<Option> boolOptions      = es.getPresetOptionsByName("YES/NO");
		Set<Option> stateOptions     = es.getPresetOptionsByName("States");
		Set<Option> oneToFiveOptions = es.getPresetOptionsByName("One to Five");
		Set<Option> sexOptions       = es.getPresetOptionsByName("Sex");

		// study

		Study study = es.createStudy();
		study.setName("Test Study");
		es.updateStudy(study);

		// ego

		// ego fields
		es.addEgoIdentifier(study, "First Name", AnswerType.TEXTUAL);
		es.addEgoIdentifier(study, "Last Name",  AnswerType.TEXTUAL);
		es.addEgoIdentifier(study, "Sex",        AnswerType.SELECTION, sexOptions);
		es.addEgoIdentifier(study, "Birth Date", AnswerType.TEXTUAL);
		es.addEgoIdentifier(study, "State",      AnswerType.SELECTION, stateOptions);

		// ego questions
		es.addEgoQuestion(study , "How many packs of cigarettes do you smoke per day?", AnswerType.NUMERICAL);
		es.addEgoQuestion(study , "Are you trying to quit?",                            AnswerType.SELECTION, boolOptions);
		es.addEgoQuestion(study , "How many years have you been trying to quit?",       AnswerType.NUMERICAL);

		// alter

		study.setAlterPrompt("Who are some people you know who smoke cigarettes?");
		es.updateStudy(study);

		// alter fields
		es.addAlterIdentifier(study, "First Name", AnswerType.TEXTUAL);
		es.addAlterIdentifier(study, "Last Name",  AnswerType.TEXTUAL);
		es.addAlterIdentifier(study, "Sex",        AnswerType.SELECTION, sexOptions);

		// alter questions
		es.addAlterQuestion(study, "Please rate each person's health from 1 to 5, with 1 being sickly and 5 being healthy.", AnswerType.SELECTION, oneToFiveOptions);

//		IdentificationPage ip = new IdentificationPage(study.getEgoIdQuestions());
//		List<Response> responses = ip.getResponses();
//		Ego e = es.findEgoByEgoIdResponses();

//		Interview i = es.conductInterview(e);
//		if (i.isComplete()) {
//			// tell user to bug off
//		}
//		if (i.isInProgress()) {
//			// tell user they may continue
//		}

//		InterviewPage inp = new InterviewPage(i);
//		inp.display();

		// finished with interview
	}
}
