// Folder Address //
package frc.robot.Input;
// Imports //
import java.util.Enumeration;
import java.util.Dictionary;
import java.util.Hashtable;

import static frc.robot.Constants.ControllerClass;
import static frc.robot.Constants.InputMaps;
import java.lang.reflect.*;
// Base Class //
public class Controller
{
    // Class Info //
    public final Class<?> controller;
    public final String className;
    // Data //
    public final Dictionary<String, Method> actionDict = new Hashtable<String, Method>();
    public final Dictionary<Method, String> resultTypes = new Hashtable<Method, String>();
    public final Dictionary<Method, Integer> methodParameters = new Hashtable<Method, Integer>();
    // Private Data //
    private static Controller controllerObject;
    private static Object actualController;
    // Constructors //
    private Controller(Class<?> controller)
    {
        this.controller = controller;
        this.className = getName();
        // Create Actual Controller //
        try 
        {
            Constructor<?> constructor = controller.getConstructor(Integer.TYPE);
            this.actualController = constructor.newInstance(0);
        }

        catch (Exception e)
        {
            e.printStackTrace();
        }
        // Connect Actions //
        connectActions();
    }

    private Controller()
    {
        // Initialize //
        this.controller = ControllerClass;
        this.className = getName();
        // Create Actual Controller //
        try 
        {
            Constructor<?> constructor = controller.getConstructor(Integer.TYPE);
            this.actualController = constructor.newInstance(0);
        }

        catch (Exception e)
        {
            e.printStackTrace();
        }
        // Connect Actions //
        connectActions();
    }
    // Private Methods //
    private void connectActions()
    {
        // Data Source //
        var inputMap = InputMaps.get(controller);
        // Data Storage //
        String[] actionNames = new String[inputMap.size()];
        String[][] methodNames = new String[inputMap.size()][3];
        // Iterate //
        int actionIndex = 0;
        for (Enumeration<String> actionEnum = inputMap.keys(); actionEnum.hasMoreElements(); actionIndex++)
        {
            // Extract Element //
            String actionName = actionEnum.nextElement();
            // Store Element //
            actionNames[actionIndex] = actionName;
        }

        actionIndex = 0;
        for (Enumeration<String[]> actionEnum = inputMap.elements(); actionEnum.hasMoreElements(); actionIndex++)
        {
            // Extract Element //
            String[] methodName = actionEnum.nextElement();
            // Store Element //
            methodNames[actionIndex] = methodName;
        }
        // Saves Actions //
        for (int i = 0; i < inputMap.size(); i++)
        {
            // Arrays //
            String[] methodData = methodNames[i];
            // Name Data //
            String actionName = actionNames[i];
            String methodName = methodData[0];
            String resultType = methodData[1];
            String parameterType = methodData.length > 2 ? methodData[2] : null;
            // Method Data //
            Method actionMethod;
            // Extract Method //
            try
            {
                actionMethod = controller.getMethod(methodName);
            }

            catch (NoSuchMethodException exception) 
            {
                // Warn //
                System.out.println("Method of name \"" + methodName + "\" not found in class " + className);
                // Fail //
                continue;
            }
            // Store Method //
            actionDict.put(actionName, actionMethod);
            resultTypes.put(actionMethod, resultType.toLowerCase());
            // Store Parameters //
            if (parameterType != null)
                methodParameters.put(actionMethod, Integer.valueOf(parameterType));
        }
    }
    
    private String getName()
    {
        // Data //
        String fullName = this.controller.getName();
        // Extracted Data //
        int beginIndex = fullName.lastIndexOf('.') + 1;
        // Return Name //
        return fullName.substring(beginIndex);
    }
    // Base Methods //
    public static Controller getController()
    {
        // Conditions //
        if (controllerObject != null)
            return controllerObject;
        // Initialize //
        controllerObject = new Controller(ControllerClass);
        // Return New Controller //
        return controllerObject;
    }
    
    public Object getActualController()
    {
        return this.actualController;
    }
    /**
     * @param actionName
     * @return is button pressed?
     */
    public boolean isActionDown(String actionName)
    {
        // Data //
        Method actionMethod = actionDict.get(actionName);
        String resultType = resultTypes.get(actionMethod);
        Object result;
        // Extract Result //
        try
        {
            // Parameter Data //
            Integer parameter = methodParameters.get(actionMethod);
            // Invoke Method //
            if (parameter == null)
                result = actionMethod.invoke(ControllerClass.cast(actualController));
            else
                result = actionMethod.invoke(ControllerClass.cast(actualController), parameter);
        }
        // Exceptions //
        catch (IllegalArgumentException exception)
        {
            // Warn //
            System.out.println("Invalid argument for action: " + actionName);
            // Fail //
            return false;
        }

        catch (InvocationTargetException exception)
        {
            // Warn //
            System.out.println("Invocation Target Exception? What the hell is that");
            System.out.println("Invalid Target?");
            // Fail //
            return false;
        }

        catch (IllegalAccessException exception)
        {
            // Warn //
            System.out.println("Attempted unauthorized access for action: " + actionName);
            // Fail //
            return false;
        }
        // Attempt Return //
        if (resultType == "boolean")
        {
            try
            {
                return (double)result > .5;
            }

            catch (ClassCastException exception)
            {
                return (boolean)result;
            }
        }

        else
        {
            // Fail //
            System.out.println("Invalid Result Type: " + resultType);
            System.out.println("Failed to attempt to extract boolean from action: " + actionName);
            return false;
        }
    }

    /**
     * @param actionName
     * POVAxis or Joystick for their  x or y Axis
     * @return Axis from -1 to 1 
     */
    public double getAxis(String actionName)
    {
        // Data //
        Method actionMethod = actionDict.get(actionName);
        String resultType = resultTypes.get(actionMethod).toLowerCase();
        Object result;
        // Extract Result //
        try
        {
            result = actionMethod.invoke(ControllerClass.cast(actualController));
        }
        // Exceptions //
        catch (IllegalArgumentException exception)
        {
            // Warn //
            System.out.println("Invalid argument for action: " + actionName);
            // Fail //
            return 0d;
        }

        catch (InvocationTargetException exception)
        {
            // Warn //
            System.out.println("Invocation Target Exception? What the hell is that");
            System.out.println("Invalid Target?");
            // Fail //
            return 0d;
        }

        catch (IllegalAccessException exception)
        {
            // Warn //
            System.out.println("Attempted unauthorized access for action: " + actionName);
            // Fail //
            return 0d;
        }

        catch (ClassCastException exception)
        {
            // Warn //
            System.out.println("Error when converting classes for action: " + actionName);
            // Fail //
            return 0d;
        }
        // Attempt Return //
        if (resultType == "double")
        {
            // Success //
            return (double)result;
        }

        else if (resultType == "xpov")
        {
            int newResult = (int)result;
            // Success //
            if (newResult == 0 || newResult == 180)
                return 0;
            else if (newResult <= 315 && newResult > 180)
                return newResult == 270 ? -1 : -.5;
            else
                return newResult == 90 ? 1 : .5;
        }

        else if (resultType == "ypov")
        {
            int newResult = (int)result;
            // Success //
            if (newResult == 90 || newResult == 270)
                return 0;
            else if (newResult >= 135 && newResult <= 225)
                return newResult == 180 ? -1 : -.5;
            else
                return newResult == 0 ? 1 : .5;
        }

        else
        {
            // Fail //
            System.out.println("Attempt to extract boolean from axis: " + actionName);
            return 0d;
        }
    }
}