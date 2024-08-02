package com.ranull.graves.event.integration.skript;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.ranull.graves.event.GraveAutoLootEvent;
import com.ranull.graves.type.Grave;
import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.EventValues;
import ch.njol.util.Checker;
import ch.njol.skript.util.Getter;

@Name("Grave Auto Loot Event")
@Description("Triggered when an entity auto loots a grave. Provides access to the entity, grave, and location.")
@Examples({
        "on grave auto loot:",
        "\tbroadcast \"Entity %event-entity% auto looted grave %event-grave% at location %event-location%\"",
})
public class EvtGraveAutoLoot extends SkriptEvent {

    static {
        Skript.registerEvent("Grave Auto Loot", EvtGraveAutoLoot.class, GraveAutoLootEvent.class, "[grave] auto loo(t|ting|ted)");

        // Registering entity values
        EventValues.registerEventValue(GraveAutoLootEvent.class, Entity.class, new Getter<Entity, GraveAutoLootEvent>() {
            @Override
            public Entity get(GraveAutoLootEvent e) {
                return e.getEntity();
            }
        }, 0);
        EventValues.registerEventValue(GraveAutoLootEvent.class, String.class, new Getter<String, GraveAutoLootEvent>() {
            @Override
            public String get(GraveAutoLootEvent e) {
                return e.getEntity() != null ? e.getEntity().getName() : null;
            }
        }, 0);
        EventValues.registerEventValue(GraveAutoLootEvent.class, String.class, new Getter<String, GraveAutoLootEvent>() {
            @Override
            public String get(GraveAutoLootEvent e) {
                return e.getEntity() != null ? e.getEntity().getUniqueId().toString() : null;
            }
        }, 0);

        // Registering grave values
        EventValues.registerEventValue(GraveAutoLootEvent.class, Grave.class, new Getter<Grave, GraveAutoLootEvent>() {
            @Override
            public Grave get(GraveAutoLootEvent e) {
                return e.getGrave();
            }
        }, 0);
        EventValues.registerEventValue(GraveAutoLootEvent.class, Location.class, new Getter<Location, GraveAutoLootEvent>() {
            @Override
            public Location get(GraveAutoLootEvent e) {
                return e.getLocation();
            }
        }, 0);

        // Registering additional grave values
        EventValues.registerEventValue(GraveAutoLootEvent.class, String.class, new Getter<String, GraveAutoLootEvent>() {
            @Override
            public String get(GraveAutoLootEvent e) {
                return e.getGrave() != null ? e.getGrave().getOwnerUUID().toString() : null;
            }
        }, 0);
        EventValues.registerEventValue(GraveAutoLootEvent.class, String.class, new Getter<String, GraveAutoLootEvent>() {
            @Override
            public String get(GraveAutoLootEvent e) {
                return e.getGrave() != null ? e.getGrave().getOwnerDisplayName() : null;
            }
        }, 0);
        EventValues.registerEventValue(GraveAutoLootEvent.class, Number.class, new Getter<Number, GraveAutoLootEvent>() {
            @Override
            public Number get(GraveAutoLootEvent e) {
                return e.getGrave() != null ? e.getGrave().getExperience() : null;
            }
        }, 0);
    }

    private Literal<Entity> entity;
    private Literal<Grave> grave;
    private Literal<Location> location;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Literal<?> @NotNull [] args, int matchedPattern, @NotNull ParseResult parseResult) {
        //entity = (Literal<Entity>) args[0];
        //grave = (Literal<Grave>) args[0];
        //location = (Literal<Location>) args[0];
        return true;
    }

    @Override
    public boolean check(Event e) {
        if (e instanceof GraveAutoLootEvent) {
            GraveAutoLootEvent event = (GraveAutoLootEvent) e;
            if (entity != null && !entity.check(event, new Checker<Entity>() {
                @Override
                public boolean check(Entity ent) {
                    return ent.equals(event.getEntity());
                }
            })) {
                return false;
            }
            if (grave != null && !grave.check(event, new Checker<Grave>() {
                @Override
                public boolean check(Grave g) {
                    return g.equals(event.getGrave());
                }
            })) {
                return false;
            }
            if (location != null && !location.check(event, new Checker<Location>() {
                @Override
                public boolean check(Location loc) {
                    return loc.equals(event.getLocation());
                }
            })) {
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public String toString(@Nullable Event e, boolean debug) {
        return "Grave auto loot event " +
                (entity != null ? " with entity " + entity.toString(e, debug) : "") +
                (grave != null ? " with grave " + grave.toString(e, debug) : "") +
                (location != null ? " at location " + location.toString(e, debug) : "");
    }
}
