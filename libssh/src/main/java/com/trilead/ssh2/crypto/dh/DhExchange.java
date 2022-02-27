package com.trilead.ssh2.crypto.dh;

import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.KeyAgreement;
import javax.crypto.interfaces.DHPrivateKey;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.DHPublicKeySpec;

/**
 * DhExchange.
 *
 * @author Christian Plattner, plattner@trilead.com, kenny
 * @version $Id: DhExchange.java,v 1.2 2008/04/01 12:38:09 cplattne Exp $
 */
public class DhExchange extends GenericDhExchange {

    /* Given by the standard */

    private static final BigInteger P1 = new BigInteger(
            "FFFFFFFFFFFFFFFFC90FDAA22168C234C4C6628B80DC1CD1" +
                    "29024E088A67CC74020BBEA63B139B22514A08798E3404DD" +
                    "EF9519B3CD3A431B302B0A6DF25F14374FE1356D6D51C245" +
                    "E485B576625E7EC6F44C42E9A637ED6B0BFF5CB6F406B7ED" +
                    "EE386BFB5A899FA5AE9F24117C4B1FE649286651ECE65381" +
                    "FFFFFFFFFFFFFFFF", 16);

    private static final BigInteger P14 = new BigInteger(
            "FFFFFFFFFFFFFFFFC90FDAA22168C234C4C6628B80DC1CD1" +
                    "29024E088A67CC74020BBEA63B139B22514A08798E3404DD" +
                    "EF9519B3CD3A431B302B0A6DF25F14374FE1356D6D51C245" +
                    "E485B576625E7EC6F44C42E9A637ED6B0BFF5CB6F406B7ED" +
                    "EE386BFB5A899FA5AE9F24117C4B1FE649286651ECE45B3D" +
                    "C2007CB8A163BF0598DA48361C55D39A69163FA8FD24CF5F" +
                    "83655D23DCA3AD961C62F356208552BB9ED529077096966D" +
                    "670C354E4ABC9804F1746C08CA18217C32905E462E36CE3B" +
                    "E39E772C180E86039B2783A2EC07A28FB5C55DF06F4C52C9" +
                    "DE2BCBF6955817183995497CEA956AE515D2261898FA0510" +
                    "15728E5A8AACAA68FFFFFFFFFFFFFFFF", 16);

    private static final BigInteger P16 = new BigInteger(
            "FFFFFFFFFFFFFFFFC90FDAA22168C234C4C6628B80DC1CD1" +
                    "29024E088A67CC74020BBEA63B139B22514A08798E3404DD" +
                    "EF9519B3CD3A431B302B0A6DF25F14374FE1356D6D51C245" +
                    "E485B576625E7EC6F44C42E9A637ED6B0BFF5CB6F406B7ED" +
                    "EE386BFB5A899FA5AE9F24117C4B1FE649286651ECE45B3D" +
                    "C2007CB8A163BF0598DA48361C55D39A69163FA8FD24CF5F" +
                    "83655D23DCA3AD961C62F356208552BB9ED529077096966D" +
                    "670C354E4ABC9804F1746C08CA18217C32905E462E36CE3B" +
                    "E39E772C180E86039B2783A2EC07A28FB5C55DF06F4C52C9" +
                    "DE2BCBF6955817183995497CEA956AE515D2261898FA0510" +
                    "15728E5A8AAAC42DAD33170D04507A33A85521ABDF1CBA64" +
                    "ECFB850458DBEF0A8AEA71575D060C7DB3970F85A6E1E4C7" +
                    "ABF5AE8CDB0933D71E8C94E04A25619DCEE3D2261AD2EE6B" +
                    "F12FFA06D98A0864D87602733EC86A64521F2B18177B200C" +
                    "BBE117577A615D6C770988C0BAD946E208E24FA074E5AB31" +
                    "43DB5BFCE0FD108E4B82D120A92108011A723C12A787E6D7" +
                    "88719A10BDBA5B2699C327186AF4E23C1A946834B6150BDA" +
                    "2583E9CA2AD44CE8DBBBC2DB04DE8EF92E8EFC141FBECAA6" +
                    "287C59474E6BC05D99B2964FA090C3A2233BA186515BE7ED" +
                    "1F612970CEE2D7AFB81BDD762170481CD0069127D5B05AA9" +
                    "93B4EA988D8FDDC186FFB7DC90A6C08F4DF435C934063199" +
                    "FFFFFFFFFFFFFFFF", 16);

    private static final BigInteger P18 = new BigInteger(
            "FFFFFFFFFFFFFFFFC90FDAA22168C234C4C6628B80DC1CD1" +
                    "29024E088A67CC74020BBEA63B139B22514A08798E3404DD" +
                    "EF9519B3CD3A431B302B0A6DF25F14374FE1356D6D51C245" +
                    "E485B576625E7EC6F44C42E9A637ED6B0BFF5CB6F406B7ED" +
                    "EE386BFB5A899FA5AE9F24117C4B1FE649286651ECE45B3D" +
                    "C2007CB8A163BF0598DA48361C55D39A69163FA8FD24CF5F" +
                    "83655D23DCA3AD961C62F356208552BB9ED529077096966D" +
                    "670C354E4ABC9804F1746C08CA18217C32905E462E36CE3B" +
                    "E39E772C180E86039B2783A2EC07A28FB5C55DF06F4C52C9" +
                    "DE2BCBF6955817183995497CEA956AE515D2261898FA0510" +
                    "15728E5A8AAAC42DAD33170D04507A33A85521ABDF1CBA64" +
                    "ECFB850458DBEF0A8AEA71575D060C7DB3970F85A6E1E4C7" +
                    "ABF5AE8CDB0933D71E8C94E04A25619DCEE3D2261AD2EE6B" +
                    "F12FFA06D98A0864D87602733EC86A64521F2B18177B200C" +
                    "BBE117577A615D6C770988C0BAD946E208E24FA074E5AB31" +
                    "43DB5BFCE0FD108E4B82D120A92108011A723C12A787E6D7" +
                    "88719A10BDBA5B2699C327186AF4E23C1A946834B6150BDA" +
                    "2583E9CA2AD44CE8DBBBC2DB04DE8EF92E8EFC141FBECAA6" +
                    "287C59474E6BC05D99B2964FA090C3A2233BA186515BE7ED" +
                    "1F612970CEE2D7AFB81BDD762170481CD0069127D5B05AA9" +
                    "93B4EA988D8FDDC186FFB7DC90A6C08F4DF435C934028492" +
                    "36C3FAB4D27C7026C1D4DCB2602646DEC9751E763DBA37BD" +
                    "F8FF9406AD9E530EE5DB382F413001AEB06A53ED9027D831" +
                    "179727B0865A8918DA3EDBEBCF9B14ED44CE6CBACED4BB1B" +
                    "DB7F1447E6CC254B332051512BD7AF426FB8F401378CD2BF" +
                    "5983CA01C64B92ECF032EA15D1721D03F482D7CE6E74FEF6" +
                    "D55E702F46980C82B5A84031900B1C9E59E7C97FBEC7E8F3" +
                    "23A97A7E36CC88BE0F1D45B7FF585AC54BD407B22B4154AA" +
                    "CC8F6D7EBF48E1D814CC5ED20F8037E0A79715EEF29BE328" +
                    "06A1D58BB7C5DA76F550AA3D8A1FBFF0EB19CCB1A313D55C" +
                    "DA56C9EC2EF29632387FE8D76E3C0468043E8F663F4860EE" +
                    "12BF2D5B0B7474D6E694F91E6DBE115974A3926F12FEE5E4" +
                    "38777CB6A932DF8CD8BEC4D073B931BA3BC832B68D9DD300" +
                    "741FA7BF8AFC47ED2576F6936BA424663AAB639C5AE4F568" +
                    "3423B4742BF1C978238F16CBE39D652DE3FDB8BEFC848AD9" +
                    "22222E04A4037C0713EB57A81A23F0C73473FC646CEA306B" +
                    "4BCBC8862F8385DDFA9D4B7FA2C087E879683303ED5BDD3A" +
                    "062B3CF5B3A278A66D2A13F83F44F82DDF310EE074AB6A36" +
                    "4597E899A0255DC164F31CC50846851DF9AB48195DED7EA1" +
                    "B1D510BD7EE74D73FAF36BC31ECFA268359046F4EB879F92" +
                    "4009438B481C6CD7889A002ED5EE382BC9190DA6FC026E47" +
                    "9558E4475677E9AA9E3050E2765694DFC81F56E880B96E71" +
                    "60C980DD98EDD3DFFFFFFFFFFFFFFFFF", 16);

    private static final BigInteger G = BigInteger.valueOf(2);

    /* Hash algorithm to use */
    private String hashAlgo;

    /* Client public and private */

    private DHPrivateKey clientPrivate;
    private DHPublicKey clientPublic;

    /* Server public */

    private DHPublicKey serverPublic;

    @Override
    public void init(String name) throws IOException {
        final DHParameterSpec spec;
        switch (name) {
            case "diffie-hellman-group18-sha512":
                spec = new DHParameterSpec(P18, G);
                hashAlgo = "SHA-512";
                break;
            case "diffie-hellman-group16-sha512":
                spec = new DHParameterSpec(P16, G);
                hashAlgo = "SHA-512";
                break;
            case "diffie-hellman-group14-sha256":
                spec = new DHParameterSpec(P14, G);
                hashAlgo = "SHA-256";
                break;
            case "diffie-hellman-group14-sha1":
                spec = new DHParameterSpec(P14, G);
                hashAlgo = "SHA-1";
                break;
            case "diffie-hellman-group1-sha1":
                spec = new DHParameterSpec(P1, G);
                hashAlgo = "SHA-1";
                break;
            default:
                throw new IllegalArgumentException("Unknown DH group " + name);
        }

        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("DH");
            kpg.initialize(spec);
            KeyPair pair = kpg.generateKeyPair();
            clientPrivate = (DHPrivateKey) pair.getPrivate();
            clientPublic = (DHPublicKey) pair.getPublic();
        } catch (NoSuchAlgorithmException e) {
            throw new IOException("No DH keypair generator", e);
        } catch (InvalidAlgorithmParameterException e) {
            throw new IOException("Invalid DH parameters", e);
        }
    }

    @Override
    public byte[] getE() {
        if (clientPublic == null)
            throw new IllegalStateException("DhExchange not initialized!");

        return clientPublic.getY().toByteArray();
    }

    @Override
    protected byte[] getServerE() {
        if (serverPublic == null)
            throw new IllegalStateException("DhExchange not initialized!");

        return serverPublic.getY().toByteArray();
    }

    @Override
    public void setF(byte[] f) throws IOException {
        if (clientPublic == null)
            throw new IllegalStateException("DhExchange not initialized!");

        final KeyAgreement ka;
        try {
            KeyFactory kf = KeyFactory.getInstance("DH");
            DHParameterSpec params = clientPublic.getParams();
            this.serverPublic = (DHPublicKey) kf.generatePublic(new DHPublicKeySpec(
                    new BigInteger(1, f), params.getP(), params.getG()));

            ka = KeyAgreement.getInstance("DH");
            ka.init(clientPrivate);
            ka.doPhase(serverPublic, true);
        } catch (NoSuchAlgorithmException e) {
            throw new IOException("No DH key agreement method", e);
        } catch (InvalidKeyException | InvalidKeySpecException e) {
            throw new IOException("Invalid DH key", e);
        }

        sharedSecret = new BigInteger(1, ka.generateSecret());
    }

    @Override
    public String getHashAlgo() {
        return hashAlgo;
    }
}
