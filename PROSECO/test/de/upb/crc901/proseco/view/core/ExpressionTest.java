package de.upb.crc901.proseco.view.core;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ExpressionTest {

	@Test
	public void Test1() {
		String exp = "a=a & (b=c|(c=c))";
		ExpressionEvaluator ev = new ExpressionEvaluator(exp);
		assertTrue(ev.evaluateExpression());
	}

	@Test
	public void Test2() {
		String exp = "a=b | (b=c | (c=c & d=d)))";
		ExpressionEvaluator ev = new ExpressionEvaluator(exp);
		assertTrue(ev.evaluateExpression());
	}

	@Test
	public void TestFalse() {
		String exp = "a=a & (b=c | (c=c & d=d)) & f=f & g=h )";
		ExpressionEvaluator ev = new ExpressionEvaluator(exp);
		assertFalse(ev.evaluateExpression());
	}

	@Test
	public void TestTrue3() {
		String exp = "a=a & (b=c | (c=c & d=d)) & f=f & g=g )";
		ExpressionEvaluator ev = new ExpressionEvaluator(exp);
		assertTrue(ev.evaluateExpression());
	}

	@Test
	public void TestNumericOperationTrue() {
		String exp = "3>1 & (!(4>=4) | 2=2 | !(3<=1))";
		ExpressionEvaluator ev = new ExpressionEvaluator(exp);
		assertTrue(ev.evaluateExpression());
	}
	
	@Test
	public void TestNumericOperationFalse() {
		String exp = "3>1 & (!(4>=4) | 2!=2 | !(3>=1))";
		ExpressionEvaluator ev = new ExpressionEvaluator(exp);
		assertFalse(ev.evaluateExpression());
	}
	
	@Test
	public void TestStringEquals(){
		String exp = "a=b";
		ExpressionEvaluator ev = new ExpressionEvaluator(exp);
		assertFalse(ev.evaluateExpression());
		
		String exp2 = "a!=b";
		ev = new ExpressionEvaluator(exp2);
		assertTrue(ev.evaluateExpression());
	}

}
