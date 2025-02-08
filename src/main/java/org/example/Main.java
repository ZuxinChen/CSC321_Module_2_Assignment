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
                    //before to compute expression, it needs check data type first
                    if(!(checkDataType(variables)== 1 ||checkDataType(variables)== 2)){
                        throw new RuntimeException("Data type of variables are not same");
                    }
                    computeExpression(variables,expression);
                }

            }


        } catch (IOException e) {
            System.out.println("Unknown Error");
        }
    }
    //check data type
    // 1 : both int
    // 2 : both double
    // decimals number: there are different
    private static double checkDataType(List<Variable> variables){
        double status = 0;
        for(Variable v: variables){
            if(v.type.equals("int")){
                status += 1;
            }else if (v.type.equals("double")){
                status += 2;
            }
        }
        return status / variables.size();
    }

    //3. compute expression
    // enter a expression and variable and print it's result
    private static void computeExpression(List<Variable> variables,String expression){
        Stack<Double> values = new Stack<>();
        Stack<Character> operators = new Stack<>();

        for (int i = 0; i < expression.length(); i++) {
            char ch = expression.charAt(i);

            //get variable like x and y
            if(Character.isLetter(ch)){
                StringBuilder variableName = new StringBuilder();
                // Collect the full variable name
                while (i < expression.length() && (Character.isLetterOrDigit(expression.charAt(i)) || expression.charAt(i) == '_')) {
                    variableName.append(expression.charAt(i));
                    i++;
                }
                i--; // go back one step, because operator will be skipped
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
            }

            //get operators
            if (ch == '+' || ch == '-' || ch == '*' || ch == '/'){
                operators.push(ch);
            }

        }

        // compute expression
        while (!operators.isEmpty()) {
            values.push(applyOperator(operators.pop(), values.pop(), values.pop()));
        }
        if(variables.get(1).type.equals("int")) {
            System.out.println("Result = " + values.pop().intValue());
        }else {
            System.out.println("Result = " + values.pop());
        }



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

