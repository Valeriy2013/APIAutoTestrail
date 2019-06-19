package api;

public enum TestStatus {
    PASSED(1),
    FAILED(5);

    private int _value;

    TestStatus(int Value) {
        this._value = Value;
    }

    public int getValue() {
        return _value;
    }

    public static TestStatus fromInt(int i) {
        for (TestStatus b : TestStatus .values()) {
            if (b.getValue() == i) { return b; }
        }
        return null;
    }
}
