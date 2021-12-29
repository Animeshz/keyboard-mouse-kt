package io.github.animeshz.kbms.entity

public sealed class MouseEvent {
    public class ButtonEvent(
        public val button: Button,
        public val down: Boolean,
        public val position: Position,
        ) : MouseEvent()

    public class MovementEvent(
        public val from: Position,
        public val to: Position,
    ) : MouseEvent()

    public class WheelEvent(
        public val down: Boolean,
    ): MouseEvent()
}
