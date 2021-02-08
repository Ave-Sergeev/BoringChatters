'use strict';

var usernamePage = document.querySelector('#username-page');
var chatPage = document.querySelector('#chat-page');
var usernameForm = document.querySelector('#usernameForm');
var messageForm = document.querySelector('#messageForm');
var messageInput = document.querySelector('#message');
var messageArea = document.querySelector('#messageArea');
var connectingElement = document.querySelector('.connecting');
var usersListArea = document.querySelector('#usersListArea');

var mySessionId = null;
var stompClient = null;
var sessionId = null;
var username = null;
var socket = null;

var colors = [
    '#2196F3', '#32c787', '#00BCD4', '#ff5652',
    '#ffc107', '#ff85af', '#FF9800', '#39bbb0'
];

//Нажатие кнопки - "Присоединится к общению"
function connect(event) {
    username = document.querySelector('#name').value.trim();

    if(username) {
        usernamePage.classList.add('hidden'); //скрытие формы ввода ника
        chatPage.classList.remove('hidden');

        socket = new SockJS('/chat.connect'); //установка endpoint
        stompClient = Stomp.over(socket);

        stompClient.connect({}, onConnected, onError);
    }
    event.preventDefault();
}

//Если успешно подключились к WebSocket
function onConnected() {
    let urlArray = socket._transport.url.split('/');
    mySessionId = urlArray[urlArray.length-2]; //вычисление номера сессии

    stompClient.subscribe('/topic/public', onMessageReceived);
    stompClient.subscribe('/topic/' + mySessionId + '/userList', onUserListReceived);
    stompClient.subscribe('/topic/lastMessages/' + mySessionId, lastMessagesReceived);

    stompClient.send("/app/users.list.req/" + mySessionId, {}, {},); // запрос списка пользователей

    connectingElement.classList.add('hidden');
}

//Если фейл подключения к WebSocket
function onError(error) {
    connectingElement.textText = 'Не удалось присоединится к WebSocket сессии, пожалуйста обновите страницу';
    connectingElement.style.color = 'red';
}

//Отправка сообщений(всем)
function send(event) {
    var messageText = messageInput.value.trim();

    if(messageText && stompClient) {
        var message = {
            author: username,
            text: messageInput.value,
            type: 'CHAT'
        };

        stompClient.send("/app/chat.send", {}, JSON.stringify(message));
        messageInput.value = '';
    }
    event.preventDefault();
}

//Вызываем при получении сообщения на '/topic/public'
function onMessageReceived(payload) {
    var message = JSON.parse(payload.body);

    addMessageToArea(message, false);
}

//Отображаем сообщения в textArea, принимаем само сообщения и флаг, смотрим в архиве ли сообщение
function addMessageToArea(message, isArchive) {
    var messageElement = document.createElement('li');

        if(message.type === 'JOIN') {
            messageElement.classList.add('event-message');
            message.text = message.author + ' вошел в чат!';
            if (!isArchive){
                addUserToUserList(message.author);
            }

        } else if (message.type === 'LEAVE') {
            messageElement.classList.add('event-message');
            message.text = message.author + ' покинул чат!';
            if (!isArchive) {
                removeUserFromUserList(message.author);
            }

        } else {
            messageElement.classList.add('chat-message');

            var avatarElement = document.createElement('i');
            var avatarText = document.createTextNode(message.author[0]);
            avatarElement.appendChild(avatarText);
            avatarElement.style['background-color'] = getAvatarColor(message.author);

            messageElement.appendChild(avatarElement);

            var usernameElement = document.createElement('span');
            var usernameText = document.createTextNode(message.author);
            usernameElement.appendChild(usernameText);
            messageElement.appendChild(usernameElement);
        }

        var textElement = document.createElement('p');
        var messageText = document.createTextNode(message.text);
        textElement.appendChild(messageText);

        messageElement.appendChild(textElement);

        messageArea.appendChild(messageElement);
        messageArea.scrollTop = messageArea.scrollHeight;
}

//Вызываем при получении списка пользователей
function onUserListReceived(payload) {
    var message = JSON.parse(payload.body);
    for(let mess in message) {
        let userName = message[mess];
        addUserToUserList(userName);
    }
    stompClient.send("/app/chat.register",
                        {},
                        JSON.stringify({author: username, type: 'JOIN'})
    ); //регистрация в чате, отправка ника, который сервер будет соотносить с сессией
}

//Добавляем юзера в список пользователей
function addUserToUserList(username) {
    var usersListElement = document.createElement('li');
    usersListElement.appendChild(document.createTextNode(username));
    usersListArea.appendChild(usersListElement);
}

//Удаляем юзера из списка пользователей
function removeUserFromUserList(username) {
    let listLength = usersListArea.children.length;

    for(let i = 0; i < listLength; i++) {
        let child = usersListArea.children[i];
        if (child.innerText == username) {
            usersListArea.removeChild(child);
            break;
        }
    }
}

//Вызываем при получении массива архивных сообщений
function lastMessagesReceived(payload) {
    let message = JSON.parse(payload.body);
    let listLength = message.length;
    for(let i = listLength-1; i >= 0; i--) {
        addMessageToArea(message[i], true);
    }
}

//Метод, формирующий цвет фона аватара
function getAvatarColor(messageSender) {
    var hash = 0;
    for (var i = 0; i < messageSender.length; i++) {
        hash = 31 * hash + messageSender.charCodeAt(i);
    }

    var index = Math.abs(hash % colors.length);
    return colors[index];
}

usernameForm.addEventListener('submit', connect, true)
messageForm.addEventListener('submit', send, true)
