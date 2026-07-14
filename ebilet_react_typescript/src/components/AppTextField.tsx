type Props = {
    value: string;
    placeholder: string;
    type?: "text" | "email" | "password" | "tel";
    onChange: (value: string) => void;
};

export function AppTextField({
                                 value,
                                 placeholder,
                                 type = "text",
                                 onChange,
                             }: Props) {
    return (
        <input
            className="app-input"
            value={value}
            placeholder={placeholder}
            type={type}
            onChange={(event) => onChange(event.target.value)}
        />
    );
}