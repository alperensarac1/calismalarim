/*
 * =========================================================
 * API HATA TİPLERİ
 * =========================================================
 */


/**
 * Alan bazlı validation hatalarını temsil eder.
 *
 * Örnek:
 *
 * {
 *     email: [
 *         "E-posta alanı zorunludur."
 *     ],
 *     password: [
 *         "Şifre en az 8 karakter olmalıdır."
 *     ]
 * }
 */
export type ApiValidationErrors =
    Record<string, string[]>;


/**
 * Ana .NET API'nin errors alanında dönebilecek
 * olası hata biçimlerini temsil eder.
 */
export type ApiErrors =
    | string[]
    | ApiValidationErrors
    | null;


/*
 * =========================================================
 * GENEL API RESPONSE TİPİ
 * =========================================================
 */


/**
 * Ana .NET API ve Python Authenticator servisinin
 * ortak response yapısını temsil eder.
 *
 * TData generic tipi sayesinde endpointin döndürdüğü
 * data alanı için farklı bir tip kullanılabilir.
 */
export interface ApiResponse<TData> {
    success: boolean;

    message: string;

    data: TData;

    errors: ApiErrors;
}


/*
 * =========================================================
 * .NET API SAYFALAMA TİPLERİ
 * =========================================================
 */


/**
 * Ana .NET API'nin kullandığı sayfalı cevap yapısıdır.
 *
 * Buradaki alanlar camelCase biçimindedir.
 *
 * Örnek:
 *
 * {
 *     items: [],
 *     page: 1,
 *     pageSize: 20,
 *     totalCount: 100,
 *     totalPages: 5,
 *     hasPreviousPage: false,
 *     hasNextPage: true
 * }
 */
export interface PagedResponse<TItem> {
    items: TItem[];

    page: number;

    pageSize: number;

    totalCount: number;

    totalPages: number;

    hasPreviousPage: boolean;

    hasNextPage: boolean;
}


/**
 * Ana .NET API'ye gönderilecek ortak sayfalama
 * parametrelerini temsil eder.
 */
export interface PaginationParams {
    page?: number;

    pageSize?: number;
}


/*
 * =========================================================
 * PYTHON AUTHENTICATOR SAYFALAMA TİPLERİ
 * =========================================================
 */


/**
 * Python FastAPI servisinin döndürdüğü sayfalama
 * meta bilgisidir.
 *
 * Python servisinden alanlar snake_case biçiminde
 * geldiği için burada da alan adlarını değiştirmeden
 * kullanıyoruz.
 */
export interface AuthenticatorPaginationMetadata {
    page: number;

    page_size: number;

    total_count: number;

    total_pages: number;

    has_previous_page: boolean;

    has_next_page: boolean;
}


/**
 * Python Authenticator servisinin sayfalı data
 * yapısını temsil eder.
 *
 * Güvenlik logu listeleme endpointi şu biçimde
 * bir data alanı döndürür:
 *
 * {
 *     items: [],
 *     pagination: {
 *         page: 1,
 *         page_size: 20,
 *         total_count: 0,
 *         total_pages: 0,
 *         has_previous_page: false,
 *         has_next_page: false
 *     }
 * }
 */
export interface AuthenticatorPagedData<TItem> {
    items: TItem[];

    pagination: AuthenticatorPaginationMetadata;
}


/**
 * Authenticator servisindeki liste endpointlerinin
 * tam response tipini oluşturmak için kullanılabilir.
 *
 * Örnek:
 *
 * type LogListResponse =
 *     AuthenticatorPagedResponse<AuthenticationLog>;
 */
export type AuthenticatorPagedResponse<TItem> =
    ApiResponse<AuthenticatorPagedData<TItem>>;


/**
 * Authenticator servisinin query string içinde
 * beklediği sayfalama parametreleridir.
 *
 * FastAPI tarafında page_size kullanıldığı için
 * burada snake_case biçimini koruyoruz.
 */
export interface AuthenticatorPaginationParams {
    page?: number;

    page_size?: number;
}


/*
 * =========================================================
 * API HATA RESPONSE TİPLERİ
 * =========================================================
 */


/**
 * API'den başarısız cevap geldiğinde Axios içerisinde
 * kullanılabilecek temel hata response tipidir.
 */
export interface ApiErrorResponse {
    success?: boolean;

    message?: string;

    errors?: ApiErrors;
}


/**
 * FastAPI normal HTTP hatalarında message yerine
 * detail alanını kullanabilir.
 *
 * Örnek:
 *
 * {
 *     detail: "Bu işlem için yetkiniz bulunmuyor."
 * }
 */
export interface FastApiErrorDetail {
    detail?: string;
}


/**
 * FastAPI validation hatasındaki tek bir hata
 * kaydını temsil eder.
 */
export interface FastApiValidationErrorItem {
    type?: string;

    loc?: Array<string | number>;

    msg?: string;

    input?: unknown;
}


/**
 * FastAPI validation hata cevabını temsil eder.
 *
 * Örnek:
 *
 * {
 *     detail: [
 *         {
 *             type: "missing",
 *             loc: ["query", "page"],
 *             msg: "Field required"
 *         }
 *     ]
 * }
 */
export interface FastApiValidationErrorResponse {
    detail?: FastApiValidationErrorItem[];
}


/**
 * Axios hatasının uygulama genelinde kullanılacak
 * sadeleştirilmiş biçimidir.
 */
export interface AppApiError {
    message: string;

    errors: string[];

    statusCode?: number;
}


/*
 * =========================================================
 * ORTAK FORM TİPLERİ
 * =========================================================
 */


/**
 * Select, Autocomplete ve benzeri bileşenlerde
 * kullanılabilecek ortak seçenek tipidir.
 */
export interface SelectOption<
    TValue extends string | number = string,
> {
    label: string;

    value: TValue;
}