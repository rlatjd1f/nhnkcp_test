package com.ksr930.nhnkcp.aop;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Aspect
@Component
public class RequestLoggingAspect {
	private static final Logger log = LoggerFactory.getLogger(RequestLoggingAspect.class);

	@Around("within(@org.springframework.web.bind.annotation.RestController *)")
	public Object logController(ProceedingJoinPoint joinPoint) throws Throwable {
		String signature = joinPoint.getSignature().toShortString();
		String args = formatArgs(joinPoint.getArgs());
		String requestInfo = resolveRequestInfo();
		log.info("[Controller] {} {} args={}", requestInfo, signature, args);
		Object result = joinPoint.proceed();
		log.info("[Controller] {} {} result={}", requestInfo, signature, safeToString(result));
		return result;
	}

	@Around("within(@org.springframework.stereotype.Service *)")
	public Object logService(ProceedingJoinPoint joinPoint) throws Throwable {
		String signature = joinPoint.getSignature().toShortString();
		String args = formatArgs(joinPoint.getArgs());
		log.info("[Service] {} args={}", signature, args);
		Object result = joinPoint.proceed();
		log.info("[Service] {} result={}", signature, safeToString(result));
		return result;
	}

	private String resolveRequestInfo() {
		RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
		if (!(attrs instanceof ServletRequestAttributes servletAttrs)) {
			return "[no-request]";
		}
		HttpServletRequest request = servletAttrs.getRequest();
        return request.getMethod() + " " + request.getRequestURI();
	}

	private String formatArgs(Object[] args) {
		if (args == null || args.length == 0) {
			return "[]";
		}
		return Arrays.stream(args)
				.map(this::safeToString)
				.collect(Collectors.joining(", ", "[", "]"));
	}

	private String safeToString(Object value) {
		if (value == null) {
			return "null";
		}
		try {
			return value.toString();
		} catch (RuntimeException ex) {
			return value.getClass().getSimpleName();
		}
	}
}
