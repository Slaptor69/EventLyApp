package com.example.eventlyapp.features.app.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventlyapp.core.presentation.SingleViewModelFactory
import com.example.eventlyapp.core.id.IdGenerator
import com.example.eventlyapp.features.app.data.PlannerRepository
import com.example.eventlyapp.features.app.domain.PlannerCommand
import com.example.eventlyapp.features.app.domain.PlannerMsg
import com.example.eventlyapp.features.app.domain.PlannerReducer
import com.example.eventlyapp.features.app.domain.PlannerState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class PlannerViewModel(
    private val reducer: PlannerReducer,
    private val repository: PlannerRepository
) : ViewModel() {
    private val _state = MutableStateFlow(PlannerState())
    val state: StateFlow<PlannerState> = _state.asStateFlow()

    init {
        accept(PlannerMsg.ScreenStarted)
    }

    fun accept(msg: PlannerMsg) {
        val update = reducer.update(_state.value, msg)
        _state.value = update.state
        update.commands.forEach { command -> execute(command) }
    }

    private fun execute(command: PlannerCommand) {
        when (command) {
            PlannerCommand.LoadPlannerSnapshot -> {
                viewModelScope.launch {
                    val snapshot = repository.loadSnapshot()
                    accept(PlannerMsg.SnapshotLoaded(snapshot))
                }
            }
            is PlannerCommand.GenerateTaskId -> {
                accept(
                    PlannerMsg.TaskIdGenerated(
                        id = IdGenerator.nextId(),
                        title = command.title,
                        description = command.description,
                        priority = command.priority,
                        isFlagged = command.isFlagged,
                        deadline = command.deadline,
                        subtasks = command.subtasks
                    )
                )
            }
            is PlannerCommand.GenerateNoteId -> {
                accept(
                    PlannerMsg.NoteIdGenerated(
                        id = IdGenerator.nextId(),
                        title = command.title,
                        text = command.text
                    )
                )
            }
            is PlannerCommand.SaveTask -> {
                viewModelScope.launch {
                    repository.saveTask(command.task)
                }
            }
            is PlannerCommand.DeleteTask -> {
                viewModelScope.launch {
                    repository.deleteTask(command.task)
                }
            }
            is PlannerCommand.SaveNote -> {
                viewModelScope.launch {
                    repository.saveNote(command.note)
                }
            }
            is PlannerCommand.DeleteNote -> {
                viewModelScope.launch {
                    repository.deleteNote(command.note)
                }
            }
        }
    }
}

class PlannerViewModelFactory @Inject constructor(
    private val reducer: PlannerReducer,
    private val repository: PlannerRepository
) : SingleViewModelFactory<PlannerViewModel>(PlannerViewModel::class.java) {
    override fun createViewModel(): PlannerViewModel = PlannerViewModel(reducer, repository)
}
