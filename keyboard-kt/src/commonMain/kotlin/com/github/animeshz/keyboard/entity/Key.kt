package com.github.animeshz.keyboard.entity

import com.github.animeshz.keyboard.ExperimentalKeyIO
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

/**
 * Represents corresponding key of the keyboard.
 *
 * [keyCode] matches with hardware scan codes.
 */
@ExperimentalJsExport
@Suppress("unused")
@ExperimentalKeyIO
@JsExport
public enum class Key(public val keyCode: Int) {
    Unknown(-1),
    Esc(1),
    Number1(2),
    Number2(3),
    Number3(4),
    Number4(5),
    Number5(6),
    Number6(7),
    Number7(8),
    Number8(9),
    Number9(10),
    Number0(11),
    Minus(12),
    Equal(13),
    Backspace(14),
    Tab(15),
    Q(16),
    W(17),
    E(18),
    R(19),
    T(20),
    Y(21),
    U(22),
    I(23),
    O(24),
    P(25),
    LeftBrace(26),
    RightBrace(27),
    Enter(28),
    LeftCtrl(29),
    A(30),
    S(31),
    D(32),
    F(33),
    G(34),
    H(35),
    J(36),
    K(37),
    L(38),
    Semicolon(39),
    Apostrophe(40),
    Backtick(41),
    LeftShift(42),
    Backslash(43),
    Z(44),
    X(45),
    C(46),
    V(47),
    B(48),
    N(49),
    M(50),
    Comma(51),
    Dot(52),
    Slash(53),
    RightShift(54),
    KeypadAsterisk(55),
    LeftAlt(56),
    Space(57),
    CapsLock(58),
    F1(59),
    F2(60),
    F3(61),
    F4(62),
    F5(63),
    F6(64),
    F7(65),
    F8(66),
    F9(67),
    F10(68),
    NumLock(69),
    ScrollLock(70),
    Keypad7(71),
    Keypad8(72),
    Keypad9(73),
    KeypadMinus(74),
    Keypad4(75),
    Keypad5(76),
    Keypad6(77),
    KeypadPlus(78),
    Keypad1(79),
    Keypad2(80),
    Keypad3(81),
    Keypad0(82),
    KeypadDot(83),
    F11(87),
    F12(88),
    KeypadEnter(96),
    RightCtrl(97),
    KeypadSlash(98),
    RightAlt(100),
    Home(102),
    Up(103),
    PageUp(104),
    Left(105),
    Right(106),
    End(107),
    Down(108),
    PageDown(109),
    Insert(110),
    Delete(111),
    Mute(113),
    VolumeDown(114),
    VolumeUp(115),
    Power(116),
    KeypadEqual(117),
    Pause(119),
    KeypadComma(121),
    LeftSuper(125),
    RightSuper(126),
    Compose(127),
    Stop(128),
    Again(129),
    Props(130),
    Undo(131),
    Front(132),
    Copy(133),
    Open(134),
    Paste(135),
    Find(136),
    Cut(137),
    Help(138),
    Menu(139),
    Calc(140),
    Setup(141),
    Sleep(142),
    Wakeup(143),
    Mail(155),
    Bookmarks(156),
    Computer(157),
    Back(158),
    Forward(159),
    NextSong(163),
    PlayPause(164),
    PreviousSong(165),
    StopCd(166),
    Record(167),
    Rewind(168),
    Phone(169),
    Refresh(173),
    F13(183),
    F14(184),
    F15(185),
    F16(186),
    F17(187),
    F18(188),
    F19(189),
    F20(190),
    F21(191),
    F22(192),
    F23(193),
    F24(194),
    ;

    override fun toString(): String {
        return "Key.$name(keyCode=$keyCode)"
    }

    public companion object {
        /**
         * 'Symbol' to Pair(Key, SHIFT_REQUIRED)
         */
        private val SYMBOL_MAPPING = mapOf(
            ' ' to Pair(Space, false),
            '~' to Pair(Backtick, true),
            '!' to Pair(Number1, true),
            '@' to Pair(Number2, true),
            '#' to Pair(Number3, true),
            '$' to Pair(Number4, true),
            '%' to Pair(Number5, true),
            '^' to Pair(Number6, true),
            '&' to Pair(Number7, true),
            '*' to Pair(Number8, true),
            '(' to Pair(Number9, true),
            ')' to Pair(Number0, true),
            '-' to Pair(Minus, false),
            '_' to Pair(Minus, true),
            '=' to Pair(Equal, false),
            '+' to Pair(Equal, true),
            '[' to Pair(LeftBrace, false),
            '{' to Pair(LeftBrace, true),
            '[' to Pair(RightBrace, false),
            '{' to Pair(RightBrace, true),
            ';' to Pair(Semicolon, false),
            ':' to Pair(Semicolon, true),
            '\'' to Pair(Apostrophe, false),
            '"' to Pair(Apostrophe, true),
            ',' to Pair(Comma, false),
            '<' to Pair(Comma, true),
            '.' to Pair(Dot, false),
            '>' to Pair(Dot, true),
            '/' to Pair(Slash, false),
            '?' to Pair(Slash, true),
        )

        private val alphabetOrdinalRange = 16..50

        /**
         * Resolves [Key] and should the Shift Key be pressed for sending the [char] to the host.
         */
        public fun fromChar(char: Char): Pair<Key, Boolean> {
            if (char == '0') return Number0 to false
            if (char in '1'..'9') return values()[char - '0' + 2] to false

            val symbolKey = SYMBOL_MAPPING[char]
            if (symbolKey != null) return symbolKey

            when (char) {
                in 'A'..'Z' -> {
                    val values = values()
                    for (i in alphabetOrdinalRange) {
                        if (values[i].name.length == 1 && values[i].name[0] == char) return values[i] to true
                    }
                }
                in 'a'..'z' -> {
                    val c = char.toUpperCase()
                    val values = values()
                    for (i in alphabetOrdinalRange) {
                        if (values[i].name.length == 1 && values[i].name[0] == c) return values[i] to false
                    }
                }
            }

            return Unknown to false
        }

        /**
         * Resolves [Key] for the given [keyCode].
         */
        public fun fromKeyCode(keyCode: Int): Key {
            val values = values()

            var start = 0
            var end = values.size

            while (start <= end) {
                val mid = (start + end) / 2

                when {
                    keyCode < values[mid].keyCode -> end = mid - 1
                    keyCode > values[mid].keyCode -> start = mid + 1
                    else -> return values[mid]
                }
            }

            return Unknown
        }
    }
}
