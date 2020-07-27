const loginForm = document.getElementById("login-form");
const loginButton = document.getElementById("login-form-submit");
const loginErrorMsg = document.getElementById("login-error-msg");

const loginEndPoint = "/riot/auth/login";

const headers = new Headers();
headers.append('Content-Type', 'application/json');

loginButton.addEventListener("click", (e) => {
    e.preventDefault();
    const username = loginForm.username.value;
    const password = loginForm.password.value;
    attemptLogin(username, password).then(response => {
        const token = response.access_token;
        alert("Login successful.")
        console.log(token)
    });
})

const attemptLogin = async (identifier, password) => {
    var raw = JSON.stringify({'identifier': identifier, 'password': password});

    var requestOptions = {
        method: 'POST',
        headers: headers,
        body: raw,
        redirect: 'follow'
    };

    return fetch(loginEndPoint, requestOptions)
        .then(response => {
            if (!response.ok) {
                throw Error(response.statusText);
            }
            return response.json();
        })
        .catch(error => /*console.log('error', error)*/alert("Login Failed."));
};
