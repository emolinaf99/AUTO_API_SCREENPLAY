package screenplay.questions;

import net.serenitybdd.rest.SerenityRest;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;

public class TheEventData implements Question<String> {

    private final String fieldName;

    public TheEventData(String fieldName) {
        this.fieldName = fieldName;
    }

    public static TheEventData field(String fieldName) {
        return new TheEventData(fieldName);
    }

    @Override
    public String answeredBy(Actor actor) {
        Object value = SerenityRest.lastResponse().body().path(fieldName);
        return value == null ? null : value.toString();
    }
}
