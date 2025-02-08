package org.example;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


class Variable {
    String type;
    String name;
    Double value;

    public Variable(String type, String name, double value) {
        this.type = type;
        this.name = name;
        this.value = value;
    }
}

public class Main {
    public static void main(String[] args) {
        File file = new File("src/main/resources/text");
        readFile(file);
    }

    //1. read text file
    public static void readFile(File file){
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            //2. find out variables and expression : using regex
            String regex1 = "^(int|double)\\s+(\\w+)\\s*=\\s*(\\d+);";
            String regex2 ="print\\(([^)]+)\\);";
            Pattern p1 = Pattern.compile(regex1);
            Pattern p2 = Pattern.compile(regex2);

            List<Variable> variables = new ArrayList<>();
            while ((line = reader.readLine()) != null) {
                Matcher m1 = p1.matcher(line);
                Matcher m2 = p2.matcher(line);
                if(m1.find()){ // find out variables
                    Variable variable = new Variable(m1.group(1),m1.group(2),Double.parseDouble(m1.group(3)));
                    variables.add(variable);
                }else if(m2.find()){ // find out expression
                    String expression = m2.group(1);
                    //Are all variables of different data types?
                    if(areAllDiff(variables)){
                        throw new RuntimeException("Data types of all variables are not same");
                    }
                    computeExpression(variables,expression);
                }
            }
        } catch (IOException e) {
            System.out.println("Unknown Error");
        }
    }

    //check data type
    // T && F = F : all int
    // F && T = F : all double
    // T && T = T : mixed types
    private static boolean areAllDiff(List<Variable> variables){
        boolean hasInt = false;
        boolean hasDouble = false;

        for (Variable v : variables) {
            if (v.type.equals("int")) {
                hasInt = true;
            } else if (v.type.equals("double")) {
                hasDouble = true;
            }

        }

        return hasInt && hasDouble;
    }

    //3. compute expression
    // enter a expression and variable and print it's result
    private static void computeExpression(List<Variable> variables,String expression){
        Stack<Double> values = new Stack<>();
        Stack<Character> operators = new Stack<>();

        // expression -> values and operators
        transformExpression(variables,expression,values,operators);

        // compute expression
        while (!operators.isEmpty()) {
            values.push(applyOperator(operators.pop(), values.pop(), values.pop()));
        }

        // print result by int or double type
        if(variables.get(1).type.equals("int")) {
            System.out.println("Result = " + values.pop().intValue());
        }else {
            System.out.println("Result = " + values.pop());
        }


    }

    //transform string Expression to two stack which able to apply
    private static void transformExpression(List<Variable> variables,String expression,Stack<Double> values,Stack<Character> operators){
        for (int i = 0; i < expression.length(); i++) {
            char ch = expression.charAt(i);

            // Skip whitespace
            if (Character.isWhitespace(ch)) {
                continue;
            }

            //get variable like x and y
            if(Character.isLetter(ch)){
                i = paresVariable(variables,expression,values,i);
                continue;
            }

            //get number
            if (Character.isDigit(ch) || ch == '.') {
                i = parseNumber(values, expression, i);
                continue;
            }

            //get operators
            if (ch == '+' || ch == '-' || ch == '*' || ch == '/') {
                parseOperator(operators, values, expression, i);
            }

        }
    }

    // get variable from expression and store it in stack value
    // return i as index in String expression
    private static int paresVariable(List<Variable> variables,String expression,Stack<Double> values,int i){
        StringBuilder variableName = new StringBuilder();
        // Collect the full variable name
        while (i < expression.length() && (Character.isLetterOrDigit(expression.charAt(i)) || expression.charAt(i) == '_')) {
            variableName.append(expression.charAt(i));
            i++;
        }
        String value = variableName.toString();

        boolean found = false;
        // make sure the variable has been declared
        for(Variable v: variables){
            if(value.equals(v.name)){
                values.push(v.value);
                found = true;
                break;
            }
        }
        if(!found){
            throw new RuntimeException("Unknown variable: " + value);
        }
        return i-1;// go back one step, because the for loop increments it
    }

    //get number from expression
    private static int parseNumber(Stack<Double> values, String expression, int i) {
        StringBuilder number = new StringBuilder();
        // Collect the full number
        while (i < expression.length() && (Character.isDigit(expression.charAt(i)) || expression.charAt(i) == '.')) {
            number.append(expression.charAt(i));
            i++;
        }
        values.push(Double.parseDouble(number.toString()));
        return i - 1; // go back one step, because the for loop increments it
    }

    // get operator from expression
    private static void parseOperator(Stack<Character> operators, Stack<Double> values, String expression, int i) {
        char ch = expression.charAt(i);
        // Check that the priority of the top-of-stack operator
        // is higher than the priority of the current operator.
        while (!operators.isEmpty() && hasPrecedence(operators.peek(), ch)) {
            values.push(applyOperator(operators.pop(), values.pop(), values.pop()));
        }
        operators.push(ch);
    }

    // Check operator precedence
    private static boolean hasPrecedence(char op1, char op2) {
        return (op1 == '*' || op1 == '/') && (op2 == '+' || op2 == '-');
    }

    // operation in variable as data type int
    private static double applyOperator(char op, double b, double a) {
        return switch (op) {
            case '+' -> a + b;
            case '-' -> a - b;
            case '*' -> a * b;
            case '/' -> {
                if (b == 0) throw new RuntimeException("Cannot divide by zero");
                yield a / b;
            }
            default -> 0;
        };
    }

}

