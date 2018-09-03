package cronapi.math;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;

import cronapi.Var;

/**
 * Classe que representa operações matemáticas
 * 
 * @author Rodrigo Santos Reis
 * @version 1.0
 * @since 2017-05-04
 *
 */
public class Operations {

	public static final Var multiply(Var... values) throws Exception {
		Var result = new Var();
		boolean isDouble = false;
		for (Var v : values)
			if (v.getType() == Var.Type.DOUBLE)
				isDouble = true;

		if (isDouble)
			result = multiplyDouble(values);
		else
			result = multiplyLong(values);
		return result;
	}

	public static final Var subtract(Var... values) throws Exception {
		Var result = new Var();
		boolean isDouble = false;
		for (Var v : values)
			if (v.getType() == Var.Type.DOUBLE)
				isDouble = true;
		if (isDouble)
			result = subtractDouble(values);
		else
			result = subtractLong(values);
		return result;
	}

	public static final Var sum(Var... values) throws Exception {
		Var result = new Var();
		boolean isDouble = false;
		for (Var v : values)
			if (v.getType() == Var.Type.DOUBLE)
				isDouble = true;
		if (isDouble)
			result = addDouble(values);
		else
			result = addLong(values);
		return result;
	}

	public static final Var listSum(Var values) throws Exception {
		return new Var(sum(values));
	}

	public static final Var addLong(Var... values) throws Exception {
		Long addedValue = 0L;
		for (Var value : values) {
			if (value.getType() == Var.Type.LIST) {

				for (Var v : value.getObjectAsList()) {
					addedValue += v.getObjectAsLong();
				}
			} else
				addedValue += value.getObjectAsLong();
		}
		return new Var(addedValue);
	}

	public static final Var addDouble(Var... values) throws Exception {
		Double addedValue = 0.0;
		for (Var value : values) {

			if (value.getType() == Var.Type.LIST) {

				for (Var v : value.getObjectAsList()) {
					addedValue += v.getObjectAsDouble();
				}
			} else
				addedValue += value.getObjectAsDouble();
		}
		return new Var(addedValue);
	}

	public static final Var subtractLong(Var... values) throws Exception {
		Long initialValue = values[0].getObjectAsLong();
		for (int i = 1; i < values.length; i++) {
			Var value = values[i];
			initialValue -= value.getObjectAsLong();
		}
		return new Var(initialValue);
	}

	public static final Var subtractDouble(Var... values) throws Exception {
		Double initialValue = values[0].getObjectAsDouble();
		for (int i = 1; i < values.length; i++) {
			Var value = values[i];
			initialValue -= value.getObjectAsDouble();
		}
		return new Var(initialValue);
	}

	public static final Var multiplyLong(Var... values) throws Exception {
		Long returnValue = 1L;
		for (Var value : values) {
			returnValue *= value.getObjectAsLong();
		}
		return new Var(returnValue);
	}

	public static final Var multiplyDouble(Var... values) throws Exception {
		Double returnValue = 1.0;
		for (Var value : values) {
			returnValue *= value.getObjectAsDouble();
		}
		return new Var(returnValue);
	}

	public static final Var divisor(Var... values) throws Exception {
		boolean isDouble = false;
		for (Var v : values)
			if (v.getType() == Var.Type.DOUBLE)
				isDouble = true;
		if (isDouble) {
			Double result = values[0].getObjectAsDouble();
			values[0] = new Var(1);
			for (Var value : values) {
				result = result / value.getObjectAsDouble();
			}
			return new Var(result);
		} else {
			Long result = values[0].getObjectAsLong();
			values[0] = new Var(1);
			for (Var value : values) {
				result = result / value.getObjectAsLong();
			}
			return new Var(result);
		}
	}

	public static final Var abs(Var value) throws Exception {
		Var result;
		switch (value.getType()) {
		case DOUBLE: {
			result = new Var(Math.abs(value.getObjectAsDouble()));
			break;
		}
		case INT: {
			result = new Var(Math.abs(value.getObjectAsLong()));
			break;
		}
		default: {
			result = new Var(Math.abs(value.getObjectAsDouble()));
		}
		}
		return result;
	}

	public static final Var sqrt(Var value) throws Exception {
		Var result;
		switch (value.getType()) {
		case DOUBLE: {
			result = new Var(Math.sqrt(Math.abs(value.getObjectAsDouble())));
			break;
		}
		case INT: {
			result = new Var(Math.sqrt(Math.abs(value.getObjectAsLong())));
			break;
		}
		default: {
			result = new Var(Math.sqrt(Math.abs(value.getObjectAsInt())));
		}
		}
		return result;
	}

	public static final Var log(Var value) throws Exception {
		Var result;
		switch (value.getType()) {
		case DOUBLE: {
			result = new Var(Math.log(value.getObjectAsDouble()));
			break;
		}
		case INT: {
			result = new Var(Math.log(value.getObjectAsLong()));
			break;
		}
		default: {
			result = new Var(Math.log(value.getObjectAsInt()));
		}
		}
		return result;
	}

	public static final Var log10(Var value) throws Exception {
		Var result;
		switch (value.getType()) {
		case DOUBLE: {
			result = new Var(Math.log10(value.getObjectAsDouble()));
			break;
		}
		case INT: {
			result = new Var(Math.log10(value.getObjectAsLong()));
			break;
		}
		default: {
			result = new Var(Math.log10(value.getObjectAsInt()));
		}
		}
		return result;
	}

	public static final Var exp(Var value) throws Exception {
		Var result;
		switch (value.getType()) {
		case DOUBLE: {
			result = new Var(Math.exp(value.getObjectAsDouble()));
			break;
		}
		case INT: {
			result = new Var(Math.exp(value.getObjectAsLong()));
			break;
		}
		default: {
			result = new Var(Math.exp(value.getObjectAsInt()));
		}
		}
		return result;
	}

	public static final Var pow(Var value1, Var value2) throws Exception {
		Var result = new Var();
		boolean isDouble = false;
		if (value1.getType() == Var.Type.DOUBLE || value2.getType() == Var.Type.DOUBLE)
			isDouble = true;
		if (isDouble)
			result = new Var(Math.pow(value1.getObjectAsDouble(), value2.getObjectAsDouble()));
		else
			result = new Var(Math.pow(value1.getObjectAsLong(), value2.getObjectAsLong()));
		return result;
	}

	public static final Var pow10(Var value1) throws Exception {
		Var result;
		switch (value1.getType()) {
		case DOUBLE: {
			result = new Var(Math.pow(10, value1.getObjectAsDouble()));
			break;
		}
		case INT: {
			result = new Var(Math.pow(10, value1.getObjectAsLong()));
			break;
		}
		default: {
			result = new Var(Math.pow(10, value1.getObjectAsDouble()));
		}
		}
		return result;
	}

	public static final Var round(Var value) throws Exception {
		Var result;
		switch (value.getType()) {
		case DOUBLE: {
			result = new Var(Math.round(value.getObjectAsDouble()));
			break;
		}
		case INT: {
			result = new Var(Math.round(value.getObjectAsLong()));
			break;
		}
		default: {
			result = new Var(Math.round(value.getObjectAsInt()));
		}
		}
		return result;
	}

	public static final Var ceil(Var value) throws Exception {
		Var result;
		switch (value.getType()) {
		case DOUBLE: {
			result = new Var(Math.ceil(value.getObjectAsDouble()));
			break;
		}
		case INT: {
			result = new Var(Math.ceil(value.getObjectAsLong()));
			break;
		}
		default: {
			result = new Var(Math.ceil(value.getObjectAsInt()));
		}
		}
		return result;
	}

	public static final Var floor(Var value) throws Exception {
		Var result;
		switch (value.getType()) {
		case DOUBLE: {
			result = new Var(Math.floor(value.getObjectAsDouble()));
			break;
		}
		case INT: {
			result = new Var(Math.floor(value.getObjectAsLong()));
			break;
		}
		default: {
			result = new Var(Math.floor(value.getObjectAsInt()));
		}
		}
		return result;
	}

	public static final Var sin(Var value) throws Exception {
		Var result;
		switch (value.getType()) {
		case DOUBLE: {
			result = new Var(Math.sin(value.getObjectAsDouble()));
			break;
		}
		case INT: {
			result = new Var(Math.sin(value.getObjectAsLong()));
			break;
		}
		default: {
			result = new Var(Math.sin(value.getObjectAsInt()));
		}
		}
		return result;
	}

	public static final Var cos(Var value) throws Exception {
		Var result;
		switch (value.getType()) {
		case DOUBLE: {
			result = new Var(Math.cos(value.getObjectAsDouble()));
			break;
		}
		case INT: {
			result = new Var(Math.cos(value.getObjectAsLong()));
			break;
		}
		default: {
			result = new Var(Math.cos(value.getObjectAsInt()));
		}
		}
		return result;
	}

	public static final Var tan(Var value) throws Exception {
		Var result;
		switch (value.getType()) {
		case DOUBLE: {
			result = new Var(Math.tan(value.getObjectAsDouble()));
			break;
		}
		case INT: {
			result = new Var(Math.tan(value.getObjectAsLong()));
			break;
		}
		default: {
			result = new Var(Math.tan(value.getObjectAsInt()));
		}
		}
		return result;
	}

	public static final Var asin(Var value) throws Exception {
		Var result;
		if (value.getObjectAsDouble() > 1.0)
			return new Var(Math.acos(1));

		switch (value.getType()) {
		case DOUBLE: {
			result = new Var(Math.asin(value.getObjectAsDouble()));
			break;
		}
		case INT: {
			result = new Var(Math.asin(value.getObjectAsLong()));
			break;
		}
		default: {
			result = new Var(Math.asin(value.getObjectAsInt()));
		}
		}
		return result;
	}

	public static final Var acos(Var value) throws Exception {
		Var result;
		if (value.getObjectAsDouble() > 1.0)
			return new Var(Math.acos(1));

		switch (value.getType()) {
		case DOUBLE: {
			result = new Var(Math.acos(value.getObjectAsDouble()));
			break;
		}
		case INT: {
			result = new Var(Math.acos(value.getObjectAsLong()));
			break;
		}
		default: {
			result = new Var(Math.acos(value.getObjectAsInt()));
		}
		}
		return result;
	}

	public static final Var atan(Var value) throws Exception {

		Var result;
		switch (value.getType()) {
		case DOUBLE: {
			result = new Var(Math.atan(value.getObjectAsDouble()));
			break;
		}
		case INT: {
			result = new Var(Math.atan(value.getObjectAsLong()));
			break;
		}
		default: {
			result = new Var(Math.atan(value.getObjectAsInt()));
		}
		}
		return result;
	}

	public static final Var pi() throws Exception {
		return new Var(Math.PI);
	}

	public static final Var neg(Var value) throws Exception {

		Var result;
		switch (value.getType()) {
		case DOUBLE: {
			result = new Var(value.getObjectAsDouble() * -1);
			break;
		}
		case INT: {
			result = new Var(value.getObjectAsLong() * -1);
			break;
		}
		default: {
			result = new Var(value.getObjectAsLong() * -1);
		}
		}
		return result;
	}

	public static final Var infinity() throws Exception {
		return new Var(Double.POSITIVE_INFINITY);
	}

	public static final Var e() throws Exception {
		return new Var(Math.E);
	}

	public static final Var goldenRatio() throws Exception {
		return new Var((1 + Math.sqrt(5)) / 2);
	}

	public static final Var isEven(Var value) throws Exception {
		if (Math.abs(value.getObjectAsInt()) % 2 == 0)
			return new Var(true);
		return new Var(false);
	}

	public static final Var isOdd(Var value) throws Exception {

		if (Math.abs(value.getObjectAsInt()) % 2 == 1)
			return new Var(true);
		return new Var(false);
	}

	public static final Var isPrime(Var value) throws Exception {
		value = new Var(Math.abs(value.getObjectAsInt()));
		if (value.getObjectAsLong() < 2)
			return new Var(false);
		if (value.getObjectAsLong() == 2)
			return new Var(true);
		if (value.getObjectAsLong() % 2 == 0)
			return new Var(false);
		for (int i = 3; i * i <= value.getObjectAsLong(); i += 2)
			if (value.getObjectAsLong() % i == 0)
				return new Var(false);
		return new Var(true);
	}

	public static final Var isInt(Var value) throws Exception {
		if (value.getType() == Var.Type.INT || value.getType() == Var.Type.NULL)
			return new Var(true);
		return new Var(false);
	}

	public static final Var isPositive(Var value) throws Exception {
		if (value.getObjectAsLong() >= 0)
			return new Var(true);
		return new Var(false);
	}

	public static final Var isNegative(Var value) throws Exception {
		if (value.getObjectAsLong() < 0)
			return new Var(true);
		return new Var(false);
	}

	public static final Var isDivisibleBy(Var value1, Var value2) throws Exception {

		if ((value1.getObjectAsDouble() % value2.getObjectAsDouble()) == 0)
			return new Var(true);
		return new Var(false);
	}

	public static final Var randomInt(Var min, Var max) throws Exception {
		Var result = new Var();
		Random random = new Random();

		boolean isDouble = false;
		if (min.getType() == Var.Type.DOUBLE || max.getType() == Var.Type.DOUBLE)
			isDouble = true;

		if (isDouble) {
			int resultado = random.nextInt(max.getObjectAsInt());
			while ((resultado < min.getObjectAsLong()) || (resultado > max.getObjectAsLong())) {
				resultado = random.nextInt(max.getObjectAsInt());
			}
			result = new Var(resultado);

		} else {
			int resultado = random.nextInt();
			while (resultado < min.getObjectAsLong() || resultado > max.getObjectAsLong()) {
				resultado = random.nextInt(max.getObjectAsInt());
			}
			result = new Var(resultado);
		}
		return result;
	}

	public static final Var randomFloat() throws Exception {
		Random random = new Random();
		double result = random.nextGaussian();
		while (result < 0.0 || result > 1.0)
			result = random.nextGaussian();
		return new Var(result);
	}

	public static final Var listSmaller(Var value) throws Exception {

		Var result;
		switch (value.getObjectAsList().getFirst().getType()) {
		case DOUBLE: {
			result = new Var(value.getObjectAsList().getFirst().getObjectAsDouble());
			for (Var v : value.getObjectAsList()) {
				if (v.getObjectAsDouble() < result.getObjectAsDouble())
					result = v;
			}
			break;
		}
		case INT: {
			result = new Var(value.getObjectAsList().getFirst().getObjectAsLong());
			for (Var v : value.getObjectAsList()) {
				if (v.getObjectAsLong() < result.getObjectAsLong())
					result = v;
			}
			break;
		}
		default: {
			result = new Var(value.getObjectAsList().getFirst().getObjectAsLong());
			for (Var v : value.getObjectAsList()) {
				if (v.getObjectAsLong() < result.getObjectAsLong())
					result = v;
			}
			break;
		}
		}
		return result;

	}

	public static final Var listLarger(Var value) throws Exception {

		Var result;
		switch (value.getObjectAsList().getFirst().getType()) {
		case DOUBLE: {
			result = new Var(value.getObjectAsList().getFirst().getObjectAsDouble());
			for (Var v : value.getObjectAsList()) {
				if (v.getObjectAsDouble() > result.getObjectAsDouble())
					result = v;
			}
			break;
		}
		case INT: {
			result = new Var(value.getObjectAsList().getFirst().getObjectAsLong());
			for (Var v : value.getObjectAsList()) {
				if (v.getObjectAsLong() > result.getObjectAsLong())
					result = v;
			}
			break;
		}
		default: {
			result = new Var(value.getObjectAsList().getFirst().getObjectAsLong());
			for (Var v : value.getObjectAsList()) {
				if (v.getObjectAsLong() > result.getObjectAsLong())
					result = v;
			}
			break;
		}
		}
		return result;

	}

	public static final Var listAverage(Var value) throws Exception {

		Var result;
		Double sum = 0.0;
		for (Var v : value.getObjectAsList()) {
			sum += v.getObjectAsDouble();
		}
		result = new Var(sum / value.size());
		return result;
	}

	public static final Var listMedium(Var value) throws Exception {

		switch (value.getObjectAsList().getFirst().getType()) {
		case DOUBLE: {

			LinkedList<Var> lklist = value.getObjectAsList();
			Collections.sort(lklist, new Comparator<Var>() {
				@Override
				public int compare(Var o1, Var o2) {
					if (o1.getObjectAsDouble() > o2.getObjectAsDouble())
						return 1;
					if (o1.getObjectAsDouble() == o2.getObjectAsDouble())
						return 0;
					if (o1.getObjectAsDouble() < o2.getObjectAsDouble())
						return -1;
					return 0;
				}
			});

			if (lklist.size() % 2 == 1)
				return new Var(lklist.get(lklist.size() / 2));
			else {
				Var result = new Var(lklist.get(lklist.size() / 2 - 1));
				result = new Var(
						(result.getObjectAsDouble() + lklist.get(lklist.size() / 2 + 1).getObjectAsDouble()) / 2);
				return result;
			}

		}
		case INT: {

			LinkedList<Var> lklist = value.getObjectAsList();
			Collections.sort(lklist, new Comparator<Var>() {

				@Override
				public int compare(Var o1, Var o2) {
					if (o1.getObjectAsLong() > o2.getObjectAsLong())
						return 1;
					if (o1.getObjectAsLong() == o2.getObjectAsLong())
						return 0;
					if (o1.getObjectAsLong() < o2.getObjectAsLong())
						return -1;
					return 0;
				}
			});

			if (lklist.size() % 2 == 1)
				return new Var(lklist.get(lklist.size() / 2));
			else {
				Var result = new Var(lklist.get(lklist.size() / 2 - 1));
				result = new Var((result.getObjectAsLong() + lklist.get(lklist.size() / 2 + 1).getObjectAsLong()) / 2);
				return result;
			}

		}
		default: {
			LinkedList<Var> lklist = value.getObjectAsList();
			Collections.sort(lklist, new Comparator<Var>() {
				@Override
				public int compare(Var o1, Var o2) {
					if (o1.getObjectAsLong() > o2.getObjectAsLong())
						return 1;
					if (o1.getObjectAsLong() == o2.getObjectAsLong())
						return 0;
					if (o1.getObjectAsLong() < o2.getObjectAsLong())
						return -1;
					return 0;
				}
			});

			if (lklist.size() % 2 == 1)
				return new Var(lklist.get(lklist.size() / 2));
			else {
				Var result = new Var(lklist.get(lklist.size() / 2 - 1));
				result = new Var((result.getObjectAsLong() + lklist.get(lklist.size() / 2 + 1).getObjectAsLong()) / 2);
				return result;
			}

		}
		}
	}

	public static final Var listModes(Var value) throws Exception {
		Var modes = new Var();
		Map<Double, Double> countMap = new HashMap<Double, Double>();
		double max = -1;
		double d;
		LinkedList<Var> ll = value.getObjectAsList();
		for (Var var : ll) {
			d = var.getObjectAsDouble();
			double count = 0;
			if (countMap.containsKey(d)) {
				count = countMap.get(d) + 1;
			} else {
				count = 1;
			}
			countMap.put(d, count);
			if (count > max) {
				max = count;
			}
		}
		for (Map.Entry<Double, Double> tuple : countMap.entrySet()) {
			if (tuple.getValue() == max) {
				modes = new Var(Var.valueOf(tuple.getKey().doubleValue()));
			}
		}
		return modes;
	}

	public static final Var listRandomItem(Var value) throws Exception {
		return new Var(value.get(randomInt(new Var(0), new Var(value.size() - 1)).getObjectAsInt()));

	}

	public static final Var listStandardDeviation(Var value) throws Exception {

		double mean = listAverage(value).getObjectAsDouble();
		double size = value.size();
		double temp = 0l;
		double d;
		LinkedList<Var> ll = value.getObjectAsList();

		if (size == 1) {
			return Var.valueOf(0.0);
		} else {
			for (Var var : ll) {
				d = var.getObjectAsDouble() - mean;
				temp = temp + d * d;
			}
		}
		return new Var(Math.sqrt(((double) 1 / (size - 1)) * temp));
	}

	public static final Var mod(Var value1, Var value2) throws Exception {
		boolean isDouble = false;
		if (value1.getType() == Var.Type.DOUBLE || value2.getType() == Var.Type.DOUBLE)
			isDouble = true;

		if (isDouble) {
			Double resultado = value1.getObjectAsDouble() % value2.getObjectAsDouble();
			return new Var(resultado);

		} else {
			Long resultado = value1.getObjectAsLong() % value2.getObjectAsLong();
			return new Var(resultado);
		}
	}

	public static final Var min(Var value1, Var value2) throws Exception {
		if (value1.getType().equals(Var.Type.DOUBLE) || value2.getType().equals(Var.Type.DOUBLE))
			return (value1.getObjectAsDouble() <= value2.getObjectAsDouble()) ? value1 : value2;
		return (value1.getObjectAsInt() <= value2.getObjectAsInt()) ? value1 : value2;

	}

	public static final Var max(Var value1, Var value2) throws Exception {
		if (value1.getType().equals(Var.Type.DOUBLE) || value2.getType().equals(Var.Type.DOUBLE))
			return (value1.getObjectAsDouble() >= value2.getObjectAsDouble()) ? value1 : value2;
		return (value1.getObjectAsInt() >= value2.getObjectAsInt()) ? value1 : value2;
	}

	public static final Var negate(Var value) throws Exception {
		if (value.getType().equals(Var.Type.DOUBLE)) {
			return new Var(value.getObjectAsDouble() * -1);
		}
		return new Var(value.getObjectAsLong() * -1);
	}
}
