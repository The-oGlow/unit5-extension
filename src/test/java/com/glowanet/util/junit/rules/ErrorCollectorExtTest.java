package com.glowanet.util.junit.rules;

public class ErrorCollectorExtTest {

//    public ErrorCollectorExt o2t;
//
//    @BeforeEach
//    public void setUp() {
//        o2t = new ErrorCollectorExt();
//    }
//
//    @Test
//    public void readErrors_initially_return_emptyList() {
//        List<Throwable> actual = o2t.readErrors();
//        assertThat(actual, notNullValue());
//        assertThat(actual, empty());
//    }
//
//    @Test
//    public void writeErrors_adds_oneErrorToList() {
//        assertThat(o2t.getErrorSize(), equalTo(0));
//        NullPointerException expected = new NullPointerException();
//        o2t.writeErrors(List.of(expected));
//        assertThat(o2t.getErrorSize(), equalTo(1));
//        assertThat(o2t.readErrors(), hasItem(expected));
//    }
//
//    @Test
//    public void getErrorSize_initially_return_zero() {
//        int actual = o2t.getErrorSize();
//        assertThat(actual, equalTo(0));
//    }
//
//    @Test
//    public void getErrorTexts_initially_return_emptyList() {
//        List<String> actual = o2t.getErrorTexts();
//        assertThat(actual, empty());
//    }
//
//    @Test
//    public void getErrorTextsToString_initially_return_emptyString() {
//        String actual = o2t.getErrorTextsToString();
//        assertThat(actual, emptyString());
//    }
//
//    @Test
//    public void reset_initially_still_zero() {
//        assertThat(o2t.getErrorSize(), equalTo(0));
//        o2t.reset();
//        assertThat(o2t.getErrorSize(), equalTo(0));
//    }
}