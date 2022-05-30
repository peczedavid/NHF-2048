![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white) ![Kotlin](https://img.shields.io/badge/kotlin-%230095D5.svg?style=for-the-badge&logo=kotlin&logoColor=white)

## :joystick: Játék kinézete
![](images/ingame.png)

## Leírás
- Egy egyszerű játék, melynek célja a csempék(tile) összeadásával eljutni egy 2048 értékűig. Ha ez sikerült, akkor nyert a játékos. Veszíteni úgy lehet, hogy még idő előtt tele lesz a pálya és nem tudunk összeadni.
- Vezérelni az ujjunk csúsztatásával lehet, 4 irányba.
- A program számon tartja az új kör kezdete óta eltelt időt, és szerzett pontokat, amit felvisz egy ranglistába is. Ezt egy relációs adatbázisba írja ki, **RoomDatabase** segítségével. A játék állását simán **SharedReferences**-ben tárolja.
- Az app a jól ismert 2048 játék klónja. A Mobil- és Webes szoftverek (VIAUAC00) tárgy keretein belül készült beadandóként.