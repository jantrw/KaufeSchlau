import { mount } from "@vue/test-utils";
import { describe, expect, it } from "vitest";
import RegionInput from "../components/RegionInput.vue";

const global = {
  stubs: {
    InputText: {
      props: ["modelValue", "invalid"],
      emits: ["update:modelValue"],
      template:
        '<input :value="modelValue" :aria-invalid="invalid ? \'true\' : \'false\'" @input="$emit(\'update:modelValue\', $event.target.value)" />',
    },
  },
};

describe("RegionInput", () => {
  it("akzeptiert fünfstellige PLZ und filtert andere Zeichen", async () => {
    const wrapper = mount(RegionInput, {
      props: { plz: "65185", region: "", required: true },
      global,
    });

    const inputs = wrapper.findAll("input");
    expect(inputs[0].attributes("aria-invalid")).toBe("false");

    await inputs[0].setValue("65a18x5");

    expect(wrapper.emitted("update:plz")?.[0]).toEqual(["65185"]);
  });

  it("markiert Teil-PLZ auch bei optionalem Standort als ungültig", () => {
    const wrapper = mount(RegionInput, {
      props: { plz: "123", region: "", required: false },
      global,
    });

    expect(wrapper.findAll("input")[0].attributes("aria-invalid")).toBe("true");
  });
});
