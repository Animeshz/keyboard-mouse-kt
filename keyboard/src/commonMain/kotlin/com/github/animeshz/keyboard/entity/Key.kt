package com.github.animeshz.keyboard.entity

import com.github.animeshz.keyboard.ExperimentalKeyIO

@Suppress("unused")
@ExperimentalKeyIO
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
    Grave(41),
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
    Rightshift(54),
    KeypadAsterisk(55),
    LeftAlt(56),
    Space(57),
    Capslock(58),
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
    LeftMeta(125),
    RightMeta(126),
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
    // TODO: Add shift-pressed keys like colon, triangular braces
    ;

    override fun toString(): String {
        return "Key.$name(keyCode=$keyCode)"
    }

    public companion object {
        // TODO: Add support for non-alphanumeric symbols
        public fun fromChar(char: Char): Key {
            if (char == '0') return Number0
            if (char in 49..57) return values()[char - '0' + 2]

            /* if (char in 65..90) */
            return values().firstOrNull { it.name.length == 1 && it.name[0] == char.toUpperCase() } ?: Unknown
        }

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
