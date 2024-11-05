package es.uvigo.mei.jwt.seguridad.jwt;

import java.util.Date;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import es.uvigo.mei.jwt.seguridad.autenticacion.UserDetailsImpl;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;

@Component
public class UtilidadesJWT {
	private static final Logger logger = LoggerFactory.getLogger(UtilidadesJWT.class);

	@Value("${ejemlojwt.clave}")
	private String clave;

	@Value("${ejemplojwt.caducidad}")
	private int caducidad;

	private SecretKey key;
	private JwtBuilder jwtBuilder;
	private JwtParser jwtParser;

	@PostConstruct
	private void inicializar() {
		this.key = Keys.hmacShaKeyFor(clave.getBytes()); // Convierte String a objeto SecretKey
		this.jwtBuilder = Jwts.builder();
		this.jwtParser = Jwts.parser().verifyWith(this.key).build();
	}

	public String crearTokenJWT(Authentication authentication) {
		UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();
		return this.crearTokenJWT(userPrincipal.getUsername());
	}

	public String crearTokenJWT(String userName) {
		return this.jwtBuilder
				.subject(userName)
				.issuedAt(new Date())
				.expiration(new Date((new Date()).getTime() + caducidad))
				.signWith(this.key, Jwts.SIG.HS256) // HMAC con SHA256
				.compact();
	}

	public String extraerLogin(String token) {
		return this.jwtParser.parseSignedClaims(token).getPayload().getSubject();
	}

	public boolean validarToken(String token) {
		try {
			this.jwtParser.parseSignedClaims(token);
			return true;
		} catch (SignatureException e) {
			logger.error("Invalid JWT signature: {}", e.getMessage());
		} catch (MalformedJwtException e) {
			logger.error("Invalid JWT token: {}", e.getMessage());
		} catch (ExpiredJwtException e) {
			logger.error("JWT token is expired: {}", e.getMessage());
		} catch (UnsupportedJwtException e) {
			logger.error("JWT token is unsupported: {}", e.getMessage());
		} catch (IllegalArgumentException e) {
			logger.error("JWT claims string is empty: {}", e.getMessage());
		}

		return false;
	}
}
