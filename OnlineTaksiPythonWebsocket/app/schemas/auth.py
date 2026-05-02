# app/schemas/auth.py

from typing import Optional
from pydantic import BaseModel, EmailStr


class RegisterRequest(BaseModel):
    full_name: str
    phone: str
    email: Optional[EmailStr] = None
    password: str
    role: str


class LoginRequest(BaseModel):
    phone: str
    password: str


class AuthResponse(BaseModel):
    access_token: str
    token_type: str
    user_id: int
    full_name: str
    role: str