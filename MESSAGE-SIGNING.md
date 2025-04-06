# INSTALLATION MESSAGE SIGNING

You should use this option if you would like to ensure strict message integrity between the client app and the backend
API. We provide installation message signing as an advanced option for situations where an additional level of integrity
assurance is required. The key pair for message signing is generated automatically when the SDK is first initialized. 
The public key is transmitted to the Approov servers to be included in Approov tokens in the `ipk` claim. The private
key never leaves the device and is held in secure hardware (e.g. TEE/Secure Enclave) to prevent the key material from
being stolen.

These additional steps require access to the [Approov CLI](https://approov.io/docs/latest/approov-cli-tool-reference/),
please follow the [installation instructions](https://approov.io/docs/latest/approov-installation/).

## ENABLING INSTALLATION MESSAGE SIGNING

Installation message signing can be enabled by executing the following command:

```shell
approov policy -setInstallPubKey on
```

This causes the public key to be included in any Approov tokens in the `ipk` claim, the presence of which then indicates
to the backend that it should expect a valid installation message signature and that this should be verified.

## ADDING THE MESSAGE SIGNATURE AUTOMATICALLY

If you are using the `ApproovService` networking stack, then Approov can automatically generate and add the message
signature. You should use this method whenever possible. You enable this by making the following call once, after
initialization:

```java
ApproovService.setApproovInterceptorExtensions(
    new ApproovDefaultMessageSigning()
        .setDefaultFactory(
            ApproovDefaultMessageSigning.generateDefaultSignatureParametersFactory()
        )
);
```

With this interceptor extension in place the Approov networking interceptor computes the request message signature and 
adds it to the request as required when the app passes attestation.

You can see a [worked example](https://github.com/approov/quickstart-android-java-okhttp/blob/master/SHAPES-EXAMPLE.md#shapes-app-with-installation-message-signing) for the Shapes app.
