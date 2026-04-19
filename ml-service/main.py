from fastapi import FastAPI
from pydantic import BaseModel

app = FastAPI()

class InputData(BaseModel):
    commit_message: str

@app.post("/predict")
def predict(data: InputData):
    if "fix" in data.commit_message.lower():
        return {"will_fail": False, "confidence": 0.7}
    else:
        return {"will_fail": True, "confidence": 0.6}