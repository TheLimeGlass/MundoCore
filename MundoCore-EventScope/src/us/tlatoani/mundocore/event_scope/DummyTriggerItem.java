package us.tlatoani.mundocore.event_scope;

import ch.njol.skript.lang.TriggerItem;
import org.bukkit.event.Event;
import us.tlatoani.mundocore.base.MundoAddon;

/**
 * Created by Tlatoani on 6/27/17.
 */
public class DummyTriggerItem extends TriggerItem {

    @Override
    protected boolean run(Event event) {
        return true;
    }

    @Override
    public String toString(Event event, boolean b) {
        return MundoAddon.name().toUpperCase() + " DUMMY TRIGGER ITEM";
    }
}
