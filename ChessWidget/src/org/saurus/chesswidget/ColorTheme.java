/*
    DroidFish - An Android chess program.
    Copyright (C) 2011  Peter Österlund, peterosterlund2@gmail.com

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.saurus.chesswidget;


public class ColorTheme {
    private static ColorTheme inst = null;

    /** Get singleton instance. */
    public static final ColorTheme instance() {
        if (inst == null)
            inst = new ColorTheme();
        return inst;
    }

    final static int DARK_SQUARE = 0;
    final static int BRIGHT_SQUARE = 1;
    final static int SELECTED_SQUARE = 2;
    final static int CURSOR_SQUARE = 3;
    final static int DARK_PIECE = 4;
    final static int BRIGHT_PIECE = 5;
    final static int CURRENT_MOVE = 6;
    final static int ARROW_0 = 7;
    final static int ARROW_1 = 8;
    final static int ARROW_2 = 9;
    final static int ARROW_3 = 10;
    final static int ARROW_4 = 11;
    final static int ARROW_5 = 12;
    final static int SQUARE_LABEL = 13;
    final static int DECORATION = 14;
    final static int PGN_COMMENT = 15;
    public final static int FONT_FOREGROUND = 16;
    public final static int GENERAL_BACKGROUND = 17;
    //private int colorTable[] = new int[numColors];
    private int themeIndex = 0;

    private final static int themeColors[][] = {
    { // Original
        0xFF808080, 0xFFBEBE5A, 0xFFFF0000, 0xFF00FF00, 0xFF000000, 0xFFFFFFFF, 0xFF888888,
        0xA01F1FFF, 0xA0FF1F1F, 0x501F1FFF, 0x50FF1F1F, 0x1E1F1FFF, 0x28FF1F1F, 0xFFFF0000,
        0xFF9F9F66, 0xFFC0C000, 0xFFF7FBC6, 0xFF292C10
    },
    { // XBoard
        0xFF77A26D, 0xFFC8C365, 0xFFFFFF00, 0xFF00FF00, 0xFF202020, 0xFFFFFFCC, 0xFF6B9262,
        0xA01F1FFF, 0xA0FF1F1F, 0x501F1FFF, 0x50FF1F1F, 0x1E1F1FFF, 0x28FF1F1F, 0xFFFF0000,
        0xFF808080, 0xFFC0C000, 0xFFEFFBBC, 0xFF28320C
    },
    { // Blue
        0xFF83A5D2, 0xFFFFFFFA, 0xFF3232D1, 0xFF5F5FFD, 0xFF282828, 0xFFF0F0F0, 0xFF3333FF,
        0xA01F1FFF, 0xA01FFF1F, 0x501F1FFF, 0x501FFF1F, 0x1E1F1FFF, 0x281FFF1F, 0xFFFF0000,
        0xFF808080, 0xFFC0C000, 0xFFFFFF00, 0xFF2E2B53
    },
    { // Grey
        0xFF666666, 0xFFDDDDDD, 0xFFFF0000, 0xFF0000FF, 0xFF000000, 0xFFFFFFFF, 0xFF888888,
        0xA01F1FFF, 0xA0FF1F1F, 0x501F1FFF, 0x50FF1F1F, 0x1E1F1FFF, 0x28FF1F1F, 0xFFFF0000,
        0xFF909090, 0xFFC0C000, 0xFFFFFFFF, 0xFF202020
    },
    { // Scid Default
        0xFF80A0A0, 0xFFD0E0D0, 0xFFFF0000, 0xFF00FF00, 0xFF000000, 0xFFFFFFFF, 0xFF666666,
        0xA01F1FFF, 0xA0FF1F1F, 0x501F1FFF, 0x50FF1F1F, 0x1E1F1FFF, 0x28FF1F1F, 0xFFFF0000,
        0xFF808080, 0xFFC0C000, 0xFFDEFBDE, 0xFF213429
    },
    { // Scid Brown
        0xB58863,   0xF0D9B5,   0xFFFF0000, 0xFF00FF00, 0xFF000000, 0xFFFFFFFF, 0xFF666666,
        0xA01F1FFF, 0xA0FF1F1F, 0x501F1FFF, 0x50FF1F1F, 0x1E1F1FFF, 0x28FF1F1F, 0xFFFF0000,
        0xFF808080, 0xFFC0C000, 0xFFF7FAE3, 0xFF40260A
    },
    { // Scid Green
        0xFF769656, 0xFFEEEED2, 0xFFFF0000, 0xFF0000FF, 0xFF000000, 0xFFFFFFFF, 0xFF666666,
        0xA01F1FFF, 0xA0FF1F1F, 0x501F1FFF, 0x50FF1F1F, 0x1E1F1FFF, 0x28FF1F1F, 0xFFFF0000,
        0xFF808080, 0xFFC0C000, 0xFFDEE3CE, 0xFF183C21
    }
    };

//    final void readColors(SharedPreferences settings) {
//        for (int i = 0; i < numColors; i++) {
//            String prefName = prefPrefix + prefNames[i];
//            String defaultColor = themeColors[defaultTheme][i];
//            String colorString = settings.getString(prefName, defaultColor);
//            colorTable[i] = 0;
//            try {
//                colorTable[i] = Color.parseColor(colorString);
//            } catch (IllegalArgumentException e) {
//            } catch (StringIndexOutOfBoundsException e) {
//            }
//        }
//    }

//    final void setTheme(SharedPreferences settings, int themeType) {
//        Editor editor = settings.edit();
//        for (int i = 0; i < numColors; i++)
//            editor.putString(prefPrefix + prefNames[i], themeColors[themeType][i]);
//        editor.commit();
//        //readColors(settings);
//    }

    public final int getColor(int colorType) {
        return themeColors[themeIndex][colorType];
    }
}
