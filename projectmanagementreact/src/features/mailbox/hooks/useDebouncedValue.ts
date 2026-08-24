import {
    useEffect,
    useState,
} from 'react';


/*
 * =========================================================
 * DEBOUNCE HOOK
 * =========================================================
 */


/**
 * Hızlı değişen bir değerin belirli bir süre boyunca
 * sabit kalmasını bekleyerek gecikmeli kopyasını üretir.
 *
 * Mailbox modülünde özellikle şu alanlarda
 * kullanılacaktır:
 *
 * - Gelen kutusu araması
 * - Gönderilenler araması
 * - Yeni mesaj alıcı araması
 *
 * Kullanıcı her karakter yazdığında API isteği
 * göndermek yerine belirtilen süre kadar beklenir.
 *
 * Örnek:
 *
 * const debouncedSearch =
 *     useDebouncedValue(
 *         searchText,
 *         400,
 *     );
 */
export function useDebouncedValue<TValue>(
    value: TValue,
    delayMilliseconds: number,
): TValue {
    const [
        debouncedValue,
        setDebouncedValue,
    ] = useState<TValue>(
        value,
    );


    useEffect(() => {
        /*
         * Yeni değer geldiğinde zamanlayıcı başlatılır.
         *
         * Belirlenen süre içerisinde value yeniden
         * değişirse önceki zamanlayıcı cleanup
         * fonksiyonunda iptal edilir.
         */
        const timeoutId =
            window.setTimeout(
                () => {
                    setDebouncedValue(
                        value,
                    );
                },

                Math.max(
                    0,
                    delayMilliseconds,
                ),
            );


        return () => {
            window.clearTimeout(
                timeoutId,
            );
        };
    }, [
        value,
        delayMilliseconds,
    ]);


    return debouncedValue;
}