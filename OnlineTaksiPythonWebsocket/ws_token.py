import requests

BASE_URL = "http://127.0.0.1:8000"

def login_and_get_token(phone: str, password: str) -> str:
    url = f"{BASE_URL}/auth/login"

    payload = {
        "phone": phone,
        "password": password
    }

    response = requests.post(url, json=payload)

    if response.status_code != 200:
        print("Login başarısız:", response.text)
        raise Exception("Login error")

    data = response.json()

    token = data["access_token"]
    print("TOKEN:", token)

    return token