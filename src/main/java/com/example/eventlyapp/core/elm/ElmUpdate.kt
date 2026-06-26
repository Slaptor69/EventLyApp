package com.example.eventlyapp.core.elm

data class ElmUpdate<State, Command, Effect>(
    val state: State,
    val commands: List<Command> = emptyList(),
    val effects: List<Effect> = emptyList()
)

fun <State, Command, Effect> State.toElmUpdate(
    commands: List<Command> = emptyList(),
    effects: List<Effect> = emptyList()
): ElmUpdate<State, Command, Effect> {
    return ElmUpdate(
        state = this,
        commands = commands,
        effects = effects
    )
}
