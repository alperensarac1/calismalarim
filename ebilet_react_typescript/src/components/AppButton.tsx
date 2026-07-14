type Props = {
    title: string;
    loading?: boolean;
    disabled?: boolean;
    color?: "blue" | "green" | "red" | "gray";
    onClick?: () => void;
};

export function AppButton({
                              title,
                              loading = false,
                              disabled = false,
                              color = "blue",
                              onClick,
                          }: Props) {
    return (
        <button
            className={`app-button app-button-${color}`}
            disabled={loading || disabled}
            onClick={onClick}
            type="button"
        >
            {loading ? "Yükleniyor..." : title}
        </button>
    );
}