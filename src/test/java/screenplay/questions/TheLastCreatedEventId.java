package screenplay.questions;

import net.serenitybdd.rest.SerenityRest;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;

public class TheLastCreatedEventId implements Question<String> {

    public static TheLastCreatedEventId value() {
        return new TheLastCreatedEventId();
    }

    @Override
    public String answeredBy(Actor actor) {
        Object value = SerenityRest.lastResponse().body().path("id");
        return value == null ? null : value.toString();
    }
}
