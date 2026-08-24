from app.security import (
    generate_one_time_code,
    hash_one_time_code,
    verify_one_time_code,
)


code = generate_one_time_code()

code_hash = hash_one_time_code(
    code,
)

print(
    f"Üretilen kod: {code}",
)

print(
    f"Hash: {code_hash}",
)

print(
    "Doğru kod sonucu:",
    verify_one_time_code(
        code,
        code_hash,
    ),
)

print(
    "Yanlış kod sonucu:",
    verify_one_time_code(
        "000000",
        code_hash,
    ),
)