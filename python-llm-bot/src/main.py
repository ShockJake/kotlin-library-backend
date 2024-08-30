from enum import Enum
from fastapi import FastAPI, HTTPException, Request, Response
import uvicorn
import ollama

import json
import secrets
import threading

app = FastAPI()


class State(Enum):
    WAITING_FOR_RESPONSE = "WAITING_FOR_RESPONSE"
    COMPLETED = "COMPLETED"
    FAILED = "FAILED"


statesMap: dict[str, State] = {}
statesLocks: dict[str, threading.Lock] = {}

responses: dict[str, str] = {}
latestResponsesLocks: dict[str, threading.Lock] = {}


def ask_bot(id: str, query: str):
    try:
        response = ollama.chat(
            model="llama3.1", messages=[{"role": "user", "content": query}]
        )
        print(
            f"[INFO] - Got a response for messaging id - {id}, duration - {response['total_duration']}"
        )
        with latestResponsesLocks[id]:
            responses[id] = response["message"]["content"]
        with statesLocks[id]:
            statesMap[id] = State.COMPLETED
    except BaseException as e:
        print(f"[ERROR] - {e}")
        with statesLocks[id]:
            statesMap[id] = State.FAILED


@app.get("/")
async def root(response: Response):
    response.headers["Access-Control-Allow-Origin"] = "*"
    return "Welcome to Library Interactive Bot!"


@app.get("/state/{id}")
async def state(id: str, response: Response):
    if id not in statesLocks.keys():
        raise HTTPException(
            status_code=401, detail={"Error": f"Message id ({id}) not found"}
        )
    with statesLocks[id]:
        response.headers["Access-Control-Allow-Origin"] = "*"
        return {"state": statesMap[id]}


@app.get("/response/{id}")
async def response(id: str, response: Response):
    if id not in latestResponsesLocks.keys():
        raise HTTPException(
            status_code=400, detail={"Error": f"Message id ({id}) not found"}
        )
    with latestResponsesLocks[id]:
        response.headers["Access-Control-Allow-Origin"] = "*"
        return {"response": responses[id]}


@app.post("/ask")
async def ask(request: Request, response: Response):
    body_bytes = await request.body()
    body_str = body_bytes.decode("utf-8")
    query = json.loads(body_str)["query"]
    id = secrets.token_urlsafe(16)

    if id not in statesLocks.keys():
        statesLocks[id] = threading.Lock()
    if id not in latestResponsesLocks.keys():
        latestResponsesLocks[id] = threading.Lock()

    with statesLocks[id]:
        statesMap[id] = State.WAITING_FOR_RESPONSE
    with latestResponsesLocks[id]:
        responses[id] = ""
    print(f"[INFO] - got a query with size {len(query)}, messaging id - {id}")
    threading.Thread(
        target=ask_bot,
        args=(
            id,
            query,
        ),
    ).start()
    response.headers["Access-Control-Allow-Origin"] = "*"
    return {"responseId": id}


def getGreetings():
    result = (
        "1. Hi, how can I help you? "
        + "2. Hi, is there anything I can do for you? "
        + "3. Hi there! Looking for something specific, or would you like some recommendations? "
        + "4. Good day! How can I make your library experience more enjoyable today? "
        + "5. Greetings! Feel free to ask if you have any questions or need assistance with anything."
    )
    return result


def getGoodbye():
    result = (
        "1. Thank you for visiting the library! Have a wonderful day! "
        + "2. Take care, and happy reading! We hope to see you again soon. "
        + "3. Goodbye! Don’t hesitate to come back if you need more assistance. "
        + "4. Have a great day! Feel free to return anytime for more great books. "
        + "5. See you next time! Enjoy your books and have a lovely day!"
    )
    return result


if __name__ == "__main__":
    print("[INFO] Application start")
    print("[INFO] Configuring model")
    try:
        configuration_response = ollama.chat(
            model="llama3.1",
            messages=[
                {
                    "role": "system",
                    "content": "You are a library assistant, "
                    + " for next prompts answer only on questions about books,"
                    + " otherwise inform a user that your knowledge "
                    + "is limited only for topics about books. "
                    + "Do not use markdown syntax and try to minimize the responses. "
                    + f"Use this phrases as a greetings: {getGreetings()} "
                    + f"And this phrases as a way to say goodbye: {getGoodbye()}",
                }
            ],
        )
        if configuration_response["done"] is not True:
            print("[ERROR] Cannot configure model...")
            print("     - Check if Ollama server is running.")
            print("     - Check if 'llama3.1' model is downloaded.")
            exit(1)

        print("[INFO] Starting server")
        uvicorn.run(app, host="0.0.0.0", port=8081)
    except BaseException as e:
        print(f"[ERROR] - {e}")
