# Home

<p>
    <a href="https://animeshz.github.io/keyboard-mouse-kt">
        <img src="https://img.shields.io/badge/Docs-You%20are%20Here-blue?style=flat-square&logo=read-the-docs" alt="Docs: Click Here" />
    </a>
    <a href="https://discord.gg/bBN9vZgcCk">
        <img src="https://img.shields.io/static/v1?label=Discord&message=Chat%20here&color=7289DA&style=flat-square&logo=discord" alt="Discord: Chat here" />
    <a href="https://github.com/Animeshz/keyboard-mouse-kt/releases">
        <img src="https://img.shields.io/github/release-date/Animeshz/keyboard-mouse-kt?style=flat-square&label=Latest%20Release" alt="Latest Release" />
    </a>
    <a href="https://bintray.com/animeshz/maven/keyboard-kt">
        <img src="https://img.shields.io/bintray/v/animeshz/maven/keyboard-kt?color=blue&style=flat-square" alt="Bintray Version">
    </a>
    <img src="https://img.shields.io/github/languages/code-size/Animeshz/keyboard-mouse-kt?style=flat-square" alt="Code Size"/>
    <a href="https://github.com/Animeshz/keyboard-mouse-kt/blob/master/LICENSE">
        <img src="https://img.shields.io/github/license/Animeshz/keyboard-mouse-kt?style=flat-square" alt="License" />
    </a>
</p>

__KeyboardMouse.kt is still in an experimental stage, as such we can't guarantee API stability between releases. While we'd love for you to try out our library, we don't recommend you use this in production just yet.__

## What is KeyboardMouse.kt

KeyboardMouse.kt is a lightweight (~60Kb per native platform, and ~70Kb on JVM), coroutine-based multiplatform kotlin library for idiomatically interacting with Keyboard and Mouse (receiving and sending global events).

We aim to provide high-level as well as high-performant low-level access to such APIs. See the usage (Keyboard/Mouse) section below to know more!

## Motivation

The most basic motivation (or use case) comes from trying to make our GUI app to hide on background and pop it back. Since most GUI frameworks only allow you to listen to events that are happening on the current window and when focus lost you don't get any, we've designed this lightweight library to easily control that.

Another not quite good but a use-case is to simulate key-presses.

We can integrate the Mouse and Keyboard API together, to for example simulate a click or something, it really depends on the personal use case.

## Contributing and future plans

The Github discussions are open! Be sure to show your existence, say hi! and share if you have any upcoming ideas :)

Issues and PRs are always welcome!

For future plans and contributing to the project please checkout [Contributing](contributing.md) section.
