# Online-Beratung LiveService
The LiveService provides a websocket (STOMP protocol) for the frontend/client. After a successful connect and subscription it sends live events to inform the client of changes or specific actions, e.g. new messages or session changes.

It also provides a REST endpoint which can be used by other services (e.g. UserService, MessageService) to trigger new live events which will then be forwarded by the LiveService to the frontend/client via websocket.

## Help and Documentation
In the project [documentation](https://onlineberatung.github.io/documentation/docs/setup/setup-backend) you'll find information for setting up and running the project.
You can find some detailled information of the service architecture and its processes in the repository [documentation](https://github.com/Onlineberatung/onlineBeratung-liveService/tree/master/documentation).

## License
The project is licensed under the AGPLv3 which you'll find [here](https://github.com/Onlineberatung/onlineBeratung-liveService/blob/master/LICENSE).

## Code of Conduct
Please have a look at our [Code of Conduct](https://github.com/Onlineberatung/.github/blob/master/CODE_OF_CONDUCT.md) before participating in the community.

## Contributing
Please read our [contribution guidelines](https://github.com/Onlineberatung/.github/blob/master/CONTRIBUTING.md) before contributing to this project.
