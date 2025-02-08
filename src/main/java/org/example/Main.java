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
                    //Are all variables have mixed data types?
                    if(hasMixedTypes(variables)){
                        throw new RuntimeException("Variables have mixed data types");
                    }
                    computeExpression(variables,expression);
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
    }

    //check data type
    // T && F = F : all int
    // F && T = F : all double
    // T && T = T : mixed types
    private static boolean hasMixedTypes(List<Variable> variables){
        boolean hasInt = false;
        boolean hasDouble = false;

        for (Variable v : variables) {
            if ("int".equals(v.type)) {
                hasInt = true;
            } else if ("double".equals(v.type)) {
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

        if(variables.isEmpty()){
            throw new RuntimeException("ERROR! variables is empty!");
        }

        // expression -> values and operators
        transformExpression(variables,expression,values,operators);

        // compute expression
        while (!operators.isEmpty()) {
            values.push(applyOperator(operators.pop(), values.pop(), values.pop()));
        }

        // print result by int or double type
        if (variables.getFirst().type.equals("int")) {
            System.out.println("Result = " + values.pop().intValue());
        } else {
            System.out.println("Result = " + values.pop());
        }



    }

    //transform string Expression to two stack which able to apply
    private static void transformExpression(List<Variable> variables,String expression,Stack<Double> values,Stack<Character> operators){
        String regex = "(\\w+|\\d+(\\.\\d*)?|[+\\-*/])";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(expression);

        while (matcher.find()) {
            String token = matcher.group();
            if (Character.isDigit(token.charAt(0)) || token.charAt(0) == '.') {
                // parse number
                values.push(Double.parseDouble(token));
            } else if (isOperator(token.charAt(0))) {
                // parse operator
                parseOperator(operators, values, token.charAt(0));
            } else {
                // parse variable
                parseVariable(variables, values, token);
            }
        }
    }

    private static boolean isOperator(char ch) {
        return ch == '+' || ch == '-' || ch == '*' || ch == '/';
    }

    // get variable from expression and store it in stack value
    private static void parseVariable(List<Variable> variables, Stack<Double> values, String variableName) {
        boolean found = false;
        for (Variable v : variables) {
            if (variableName.equals(v.name)) {
                values.push(v.value);
                found = true;
                break;
            }
        }
        if (!found) {
            throw new RuntimeException("Unknown variable: " + variableName);
        }
    }

    // get operator from expression and store it in stack operators
    // and note that operator has precedence
    private static void parseOperator(Stack<Character> operators, Stack<Double> values, char ch) {
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

    // apply operation with value
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

