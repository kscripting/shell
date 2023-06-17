@echo off

IF "-f"=="%1" (
    type %2
    exit
)

echo %1
