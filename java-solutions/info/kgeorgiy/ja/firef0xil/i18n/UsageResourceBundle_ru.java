package info.kgeorgiy.ja.firef0xil.i18n;

import java.util.ListResourceBundle;

public class UsageResourceBundle_ru extends ListResourceBundle {
    private final static Object[][] CONTENTS = {
            {"analyzedFile", "Анализируемый файл"},
            {"allStats", "Сводная статистика"},
            {"sentencesStats", "Статистика по предложениям"},
            {"wordsStats", "Статистика по словам"},
            {"numbersStats", "Статистика по числам"},
            {"datesStats", "Статистика по датам"},
            {"currenciesStats", "Статистика по суммам денег"},
            {"sentence", "предложение"},
            {"sentences", "предложений"},
            {"Number", "Число"},
            {"number", "чисел"},
            {"word", "слово"},
            {"words", "слова"},
            {"word-o", ""},
            {"Average", "Средняя"},
            {"average", "Среднее"},
            {"date", "дата"},
            {"Min-o", "Минимальное"},
            {"Min-a", "Минимальная"},
            {"Max-o", "Максимальное"},
            {"Max-a", "Максимальная"},
            {"unique", "различных"},
            {"length", "длина"},
            {"sum", "сумм"},
            {"sum-a", "сумма"},
            {"", ""},

            /*
                    Число чисел:40.
    дат:3.
    предложения:55,465.
    слов:275(157различных).
    слово:"GK".
    слова:6,72.
    Число чисел:40(24различных).
    число "

    */
    };


    @Override
    protected Object[][] getContents() {
        return CONTENTS;
    }
}
